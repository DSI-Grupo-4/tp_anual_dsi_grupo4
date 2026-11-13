#!/bin/bash
# Deja el realm donatrack con la configuración de la Entrega 6 aunque ya
# exista en la base (el import de donatrack-realm.json solo corre la primera vez).
# Es idempotente: se puede correr en cada "up" del perfil auth.
# No borra usuarios, clientes ni el realm. Los secrets salen del entorno
# (keycloak/.env) y nunca se imprimen.
set -euo pipefail
# Sin esto, un error dentro de $(...) no corta el script.
shopt -s inherit_errexit

KCADM=/opt/keycloak/bin/kcadm.sh
CONFIG=/tmp/kcadm.config
REALM=donatrack
SERVIDOR=${KEYCLOAK_SERVIDOR_INTERNO:-http://keycloak:8080}
WEB_URL=${DONATRACK_WEB_URL:-http://localhost:8085}

kc() { "$KCADM" "$@" --config "$CONFIG"; }

falla() { echo "configurar-realm: $*" >&2; exit 1; }

validar_secret() {
  # $1 nombre de la variable, $2 valor. Vacío = no se toca el secret actual.
  if [ -n "$2" ] && ! [[ "$2" =~ ^[A-Za-z0-9._~-]{16,}$ ]]; then
    falla "$1 tiene que tener al menos 16 caracteres entre letras, números y . _ ~ -"
  fi
}

[ -n "${KEYCLOAK_ADMIN_PASSWORD:-}" ] || falla "falta KEYCLOAK_ADMIN_PASSWORD en keycloak/.env"
validar_secret KEYCLOAK_WEB_CLIENT_SECRET "${KEYCLOAK_WEB_CLIENT_SECRET:-}"
validar_secret KEYCLOAK_DONACIONES_CLIENT_SECRET "${KEYCLOAK_DONACIONES_CLIENT_SECRET:-}"
validar_secret KEYCLOAK_INCENTIVOS_CLIENT_SECRET "${KEYCLOAK_INCENTIVOS_CLIENT_SECRET:-}"

ADMIN_USERNAME=${KEYCLOAK_ADMIN_USERNAME:-admin}
if ! kc config credentials --server "$SERVIDOR" --realm master \
  --user "$ADMIN_USERNAME" --password "$KEYCLOAK_ADMIN_PASSWORD" >/dev/null 2>&1; then
  # KEYCLOAK_ADMIN_PASSWORD is only a bootstrap setting: an existing Keycloak
  # database keeps the old password. During a local credential rotation, use
  # the previous value once, update the persisted admin, and continue with the
  # new value. The launcher removes KEYCLOAK_ADMIN_PASSWORD_PREVIOUS after this
  # configuration succeeds.
  [ -n "${KEYCLOAK_ADMIN_PASSWORD_PREVIOUS:-}" ] || falla "no se pudo autenticar con KEYCLOAK_ADMIN_PASSWORD; para una instancia existente, configurar KEYCLOAK_ADMIN_PASSWORD_PREVIOUS una vez"
  kc config credentials --server "$SERVIDOR" --realm master \
    --user "$ADMIN_USERNAME" --password "$KEYCLOAK_ADMIN_PASSWORD_PREVIOUS" >/dev/null
  kc set-password -r master --username "$ADMIN_USERNAME" \
    --new-password "$KEYCLOAK_ADMIN_PASSWORD" >/dev/null
  kc config credentials --server "$SERVIDOR" --realm master \
    --user "$ADMIN_USERNAME" --password "$KEYCLOAK_ADMIN_PASSWORD" >/dev/null
fi

# --- Realm: tema, idioma, registro con verificación de email (Mailpit) ---
kc update "realms/$REALM" \
  -s loginTheme=donatrack \
  -s internationalizationEnabled=true \
  -s 'supportedLocales=["es"]' \
  -s defaultLocale=es \
  -s registrationAllowed=true \
  -s registrationEmailAsUsername=true \
  -s verifyEmail=true \
  -s resetPasswordAllowed=true \
  -s loginWithEmailAllowed=true \
  -s duplicateEmailsAllowed=false \
  -s actionTokenGeneratedByUserLifespan=1800

# --- SMTP: interruptor explícito, no "¿están las credenciales cargadas?" ---
# KEYCLOAK_SMTP_REAL=false (default): Mailpit local, no gasta cuota de Gmail.
# KEYCLOAK_SMTP_REAL=true: Gmail real con KEYCLOAK_SMTP_USERNAME/PASSWORD
# (misma cuenta que usa Notificaciones, ver servicio-notificaciones/.env).
# Las credenciales pueden quedar siempre cargadas en keycloak/.env: alcanza
# con tocar este booleano para pasar de probar a producción y volver.
if [ "${KEYCLOAK_SMTP_REAL:-false}" = "true" ]; then
  [ -n "${KEYCLOAK_SMTP_USERNAME:-}" ] && [ -n "${KEYCLOAK_SMTP_PASSWORD:-}" ] \
    || falla "KEYCLOAK_SMTP_REAL=true pero faltan KEYCLOAK_SMTP_USERNAME/KEYCLOAK_SMTP_PASSWORD en keycloak/.env"
  kc update "realms/$REALM" \
    -s "smtpServer={\"host\":\"smtp.gmail.com\",\"port\":\"587\",\"from\":\"$KEYCLOAK_SMTP_USERNAME\",\"fromDisplayName\":\"DonaTrack\",\"auth\":\"true\",\"user\":\"$KEYCLOAK_SMTP_USERNAME\",\"password\":\"$KEYCLOAK_SMTP_PASSWORD\",\"ssl\":\"false\",\"starttls\":\"true\"}"
  echo "SMTP real configurado ($KEYCLOAK_SMTP_USERNAME): los mails de Keycloak salen por Gmail, no por Mailpit."
else
  kc update "realms/$REALM" \
    -s 'smtpServer={"host":"mailpit","port":"1025","from":"no-responder@donatrack.local","fromDisplayName":"DonaTrack","auth":"false","ssl":"false","starttls":"false"}'
  echo "SMTP de prueba (Mailpit, http://localhost:8025). KEYCLOAK_SMTP_REAL=true en keycloak/.env para Gmail real."
fi

# donante_id y entidad_ids los escribe servicio-donaciones por la API de
# administración; el usuario no los ve ni los puede editar.
kc update "realms/$REALM/users/profile" -s unmanagedAttributePolicy=ADMIN_EDIT

for rol in ADMIN DONANTE BENEFICIARIA; do
  if ! kc get "roles/$rol" -r "$REALM" >/dev/null 2>&1; then
    kc create roles -r "$REALM" -s name="$rol" >/dev/null
  fi
done

# --- Clientes ---
id_cliente() {
  kc get clients -r "$REALM" -q clientId="$1" --fields id --format csv --noquotes
}

# $1 clientId, $2 JSON del cliente, $3 secret (opcional)
asegurar_cliente() {
  local json="$2" id
  if [ -n "$3" ]; then
    json="${json%\}},\"secret\":\"$3\"}"
  fi
  id=$(id_cliente "$1")
  if [ -z "$id" ]; then
    id=$(printf '%s' "$json" | kc create clients -r "$REALM" -f - -i)
  else
    printf '%s' "$json" | kc update "clients/$id" -r "$REALM" --merge -f - >/dev/null
  fi
  printf '%s' "$id"
}

# $1 id interno del cliente, $2 nombre del mapper, $3 JSON del mapper
asegurar_mapper() {
  if kc get "clients/$1/protocol-mappers/models" -r "$REALM" --fields name --format csv --noquotes | grep -xF "$2" >/dev/null; then
    return 0
  fi
  printf '%s' "$3" | kc create "clients/$1/protocol-mappers/models" -r "$REALM" -f - >/dev/null
}

WEB_ID=$(asegurar_cliente servicio-web "{
  \"clientId\":\"servicio-web\",\"name\":\"Cliente web de DonaTrack\",\"enabled\":true,
  \"protocol\":\"openid-connect\",\"publicClient\":false,
  \"standardFlowEnabled\":true,\"implicitFlowEnabled\":false,\"directAccessGrantsEnabled\":false,
  \"serviceAccountsEnabled\":true,\"frontchannelLogout\":true,
  \"rootUrl\":\"$WEB_URL\",
  \"redirectUris\":[\"$WEB_URL/login/oauth2/code/keycloak\",\"$WEB_URL/ingresar\"],
  \"webOrigins\":[\"$WEB_URL\"],
  \"attributes\":{\"post.logout.redirect.uris\":\"$WEB_URL##$WEB_URL/\"}
}" "${KEYCLOAK_WEB_CLIENT_SECRET:-}")

asegurar_mapper "$WEB_ID" "roles de realm" '{
  "name":"roles de realm","protocol":"openid-connect","protocolMapper":"oidc-usermodel-realm-role-mapper",
  "config":{"claim.name":"realm_access.roles","multivalued":"true","jsonType.label":"String",
    "id.token.claim":"true","access.token.claim":"true","userinfo.token.claim":"true"}}'
asegurar_mapper "$WEB_ID" "donante_id" '{
  "name":"donante_id","protocol":"openid-connect","protocolMapper":"oidc-usermodel-attribute-mapper",
  "config":{"user.attribute":"donante_id","claim.name":"donante_id","jsonType.label":"String","multivalued":"false",
    "id.token.claim":"true","access.token.claim":"true","userinfo.token.claim":"true"}}'
asegurar_mapper "$WEB_ID" "entidad_ids" '{
  "name":"entidad_ids","protocol":"openid-connect","protocolMapper":"oidc-usermodel-attribute-mapper",
  "config":{"user.attribute":"entidad_ids","claim.name":"entidad_ids","jsonType.label":"String","multivalued":"true",
    "id.token.claim":"true","access.token.claim":"true","userinfo.token.claim":"true"}}'

# Swagger local: cliente público, sin secret ni permisos propios. Usa los roles del usuario.
asegurar_cliente swagger-ui '{
  "clientId":"swagger-ui","name":"Swagger local DonaTrack","enabled":true,
  "protocol":"openid-connect","publicClient":true,"standardFlowEnabled":true,
  "implicitFlowEnabled":false,"directAccessGrantsEnabled":false,"serviceAccountsEnabled":false,
  "redirectUris":["http://localhost:8080/swagger-ui/oauth2-redirect.html","http://localhost:8081/swagger-ui/oauth2-redirect.html","http://localhost:8082/swagger-ui/oauth2-redirect.html","http://localhost:8083/swagger-ui/oauth2-redirect.html"],
  "webOrigins":["http://localhost:8080","http://localhost:8081","http://localhost:8082","http://localhost:8083"],
  "attributes":{"pkce.code.challenge.method":"S256"}
}' "" >/dev/null

SERVICIO='"protocol":"openid-connect","publicClient":false,"standardFlowEnabled":false,"implicitFlowEnabled":false,"directAccessGrantsEnabled":false,"serviceAccountsEnabled":true'
asegurar_cliente servicio-donaciones "{\"clientId\":\"servicio-donaciones\",\"name\":\"Servicio de Donaciones\",\"enabled\":true,$SERVICIO}" \
  "${KEYCLOAK_DONACIONES_CLIENT_SECRET:-}" >/dev/null
asegurar_cliente servicio-incentivos "{\"clientId\":\"servicio-incentivos\",\"name\":\"Servicio de Incentivos\",\"enabled\":true,$SERVICIO}" \
  "${KEYCLOAK_INCENTIVOS_CLIENT_SECRET:-}" >/dev/null

# Donaciones crea o completa las cuentas de donantes y representantes.
# Keycloak 26.8 exige el scope y la audiencia administrativos, además de los roles.
# Como scope default, también se incluye al renovar los tokens client_credentials.
DONACIONES_ID=$(id_cliente servicio-donaciones)
ADMIN_SCOPE_ID=$(kc get client-scopes -r "$REALM" --fields id,name --format csv --noquotes |
  while IFS=, read -r scope_id scope_name; do
    if [ "$scope_name" = "kc-admin-api" ]; then printf '%s' "$scope_id"; fi
  done)
[ -n "$ADMIN_SCOPE_ID" ] || falla "no existe el client scope kc-admin-api requerido por la API administrativa"
kc update "clients/$DONACIONES_ID/default-client-scopes/$ADMIN_SCOPE_ID" -r "$REALM" >/dev/null
kc add-roles -r "$REALM" --uusername service-account-servicio-donaciones --cclientid realm-management \
  --rolename manage-users --rolename view-users --rolename query-users --rolename view-realm

# --- Google (opcional) ---
if [ -n "${GOOGLE_CLIENT_ID:-}" ] && [ -n "${GOOGLE_CLIENT_SECRET:-}" ]; then
  [[ "$GOOGLE_CLIENT_ID" =~ ^[A-Za-z0-9._-]+$ ]] || falla "GOOGLE_CLIENT_ID tiene caracteres inesperados"
  [[ "$GOOGLE_CLIENT_SECRET" =~ ^[A-Za-z0-9._-]+$ ]] || falla "GOOGLE_CLIENT_SECRET tiene caracteres inesperados"
  GOOGLE="{
    \"alias\":\"google\",\"providerId\":\"google\",\"displayName\":\"Google\",\"enabled\":true,
    \"trustEmail\":true,\"storeToken\":false,\"linkOnly\":false,
    \"firstBrokerLoginFlowAlias\":\"first broker login\",
    \"config\":{\"clientId\":\"$GOOGLE_CLIENT_ID\",\"clientSecret\":\"$GOOGLE_CLIENT_SECRET\",
      \"defaultScope\":\"openid profile email\",\"syncMode\":\"IMPORT\",
      \"prompt\":\"select_account\"}}"
  if kc get identity-provider/instances/google -r "$REALM" >/dev/null 2>&1; then
    printf '%s' "$GOOGLE" | kc update identity-provider/instances/google -r "$REALM" -f - >/dev/null
  else
    printf '%s' "$GOOGLE" | kc create identity-provider/instances -r "$REALM" -f - >/dev/null
  fi
  echo "Google configurado como proveedor de identidad."
else
  echo "Sin GOOGLE_CLIENT_ID/GOOGLE_CLIENT_SECRET: no se configura el ingreso con Google."
fi

for variable in KEYCLOAK_WEB_CLIENT_SECRET KEYCLOAK_DONACIONES_CLIENT_SECRET KEYCLOAK_INCENTIVOS_CLIENT_SECRET; do
  if [ -z "${!variable:-}" ]; then
    echo "Sin $variable: el cliente conserva el secret que ya tenía (verlo en la consola, pestaña Credentials)."
  fi
done
echo "Realm $REALM configurado."
