#!/usr/bin/env bash
#
# Compila y levanta los servicios implementados y la infraestructura local.
# Si existe keycloak/.env, también inicia Keycloak y configura el realm.
#
# Uso: ./run-servicios.sh [build|up [--sin-keycloak]|down|status|logs <servicio>]
#   (sin argumentos equivale a "up")

set -uo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$ROOT_DIR/logs"
PID_DIR="$ROOT_DIR/.pids"
INCENTIVOS_DIR="$ROOT_DIR/servicio-incentivos"
COMPOSE_FILE="$ROOT_DIR/docker-compose.integration.yml"
KEYCLOAK_ENV="$ROOT_DIR/keycloak/.env"

mkdir -p "$LOG_DIR" "$PID_DIR"

# servicio-donaciones y servicio-logistica comparten el puerto 8080 por
# defecto; este lanzador asigna puertos distintos sin tocar sus propiedades.
declare -A PORTS=(
  [servicio-donaciones]=8080
  [servicio-logistica]=8083
  [servicio-incentivos]=8081
  [servicio-notificaciones]=8082
  [servicio-web]=8085
)
SERVICE_ORDER=(servicio-donaciones servicio-logistica servicio-incentivos servicio-notificaciones servicio-web)

is_implemented() {
  find "$ROOT_DIR/$1/src/main/java" -name "*.java" 2>/dev/null \
    | xargs -r grep -l "public static void main" >/dev/null 2>&1
}

implemented_services() {
  for name in "${SERVICE_ORDER[@]}"; do
    is_implemented "$name" && echo "$name"
  done
}

usage() {
  cat <<EOF
Uso: $0 <comando>

Comandos:
  build                       Compila (mvn package) los servicios implementados
  up [--sin-keycloak]         Levanta infraestructura y servicios (default)
  down                        Detiene servicios e infraestructura
  status                      Muestra servicios e infraestructura
  logs <servicio>             Sigue logs; servicios Docker: n8n, mysql, rabbitmq,
                              adminer, mailpit y keycloak

Si existe keycloak/.env, 'up' inicia Keycloak, configura el realm y activa
SEGURIDAD_HABILITADA en los servicios que no la definan en su propio .env.
EOF
}

build() {
  local failed=()
  for name in $(implemented_services); do
    echo "==> Compilando $name..."
    if mvn -q -pl "$name" -am -DskipTests package > "$LOG_DIR/$name-build.log" 2>&1; then
      echo "    OK"
    else
      echo "    FALLÓ (ver $LOG_DIR/$name-build.log)"
      failed+=("$name")
    fi
  done
  if [[ ${#failed[@]} -gt 0 ]]; then
    echo "No se pudieron compilar: ${failed[*]}"
    return 1
  fi
}

find_jar() {
  find "$ROOT_DIR/$1/target" -maxdepth 1 -name "*.jar" \
    ! -name "*-sources.jar" ! -name "*.original" 2>/dev/null | head -n1
}

# Lee KEY=VALUE sin evaluar el contenido como código de shell. Acepta valores
# delimitados por comillas y CRLF para que los .env funcionen igual en Windows.
load_env_file() {
  local file="$1" line key value
  [[ -f "$file" ]] || return 0
  while IFS= read -r line || [[ -n "$line" ]]; do
    line="${line%$'\r'}"
    [[ "$line" =~ ^[[:space:]]*$ || "$line" =~ ^[[:space:]]*# ]] && continue
    if [[ "$line" =~ ^[[:space:]]*([A-Za-z_][A-Za-z0-9_]*)[[:space:]]*=(.*)$ ]]; then
      key="${BASH_REMATCH[1]}"
      value="${BASH_REMATCH[2]}"
      if [[ ${#value} -ge 2 && ( ( "${value:0:1}" == '"' && "${value: -1}" == '"' ) || ( "${value:0:1}" == "'" && "${value: -1}" == "'" ) ) ]]; then
        value="${value:1:${#value}-2}"
      fi
      printf -v "$key" '%s' "$value"
      export "$key"
    fi
  done < "$file"
}

get_env_value() {
  local file="$1" wanted="$2" line key value
  ENV_VALUE=""
  [[ -f "$file" ]] || return 0
  while IFS= read -r line || [[ -n "$line" ]]; do
    line="${line%$'\r'}"
    [[ "$line" =~ ^[[:space:]]*# || "$line" =~ ^[[:space:]]*$ ]] && continue
    if [[ "$line" =~ ^[[:space:]]*([A-Za-z_][A-Za-z0-9_]*)[[:space:]]*=(.*)$ ]]; then
      key="${BASH_REMATCH[1]}"
      [[ "$key" == "$wanted" ]] || continue
      value="${BASH_REMATCH[2]}"
      if [[ ${#value} -ge 2 && ( ( "${value:0:1}" == '"' && "${value: -1}" == '"' ) || ( "${value:0:1}" == "'" && "${value: -1}" == "'" ) ) ]]; then
        value="${value:1:${#value}-2}"
      fi
      ENV_VALUE="$value"
      return 0
    fi
  done < "$file"
}

validate_keycloak_env() {
  local key value service pair
  for key in KEYCLOAK_ADMIN_PASSWORD KEYCLOAK_DB_PASSWORD KEYCLOAK_DB_ROOT_PASSWORD; do
    get_env_value "$KEYCLOAK_ENV" "$key"
    value="$ENV_VALUE"
    if [[ -z "$value" || "$value" == definir-* ]]; then
      echo "Falta configurar $key en keycloak/.env; no se inicia Keycloak." >&2
      return 1
    fi
  done
  for pair in KEYCLOAK_WEB_CLIENT_SECRET:servicio-web KEYCLOAK_DONACIONES_CLIENT_SECRET:servicio-donaciones KEYCLOAK_INCENTIVOS_CLIENT_SECRET:servicio-incentivos; do
    key="${pair%%:*}"
    service="${pair#*:}"
    get_env_value "$KEYCLOAK_ENV" "$key"
    value="$ENV_VALUE"
    if [[ -z "$value" ]]; then
      get_env_value "$ROOT_DIR/$service/.env" KEYCLOAK_CLIENT_SECRET
      value="$ENV_VALUE"
    fi
    if [[ ! "$value" =~ ^[A-Za-z0-9._~-]{16,}$ || "$value" == definir-* || "$value" == pegar-* ]]; then
      echo "Configurá un KEYCLOAK_CLIENT_SECRET válido para $service (en keycloak/.env o en su .env local)." >&2
      return 1
    fi
  done
}

remove_previous_admin_password() {
  grep -q '^[[:space:]]*KEYCLOAK_ADMIN_PASSWORD_PREVIOUS=' "$KEYCLOAK_ENV" 2>/dev/null || return 0
  local temp_file
  temp_file="$(mktemp "$ROOT_DIR/keycloak/.env.XXXXXX")" || return 1
  awk '!/^[[:space:]]*KEYCLOAK_ADMIN_PASSWORD_PREVIOUS=/' "$KEYCLOAK_ENV" > "$temp_file"
  chmod 600 "$temp_file"
  mv "$temp_file" "$KEYCLOAK_ENV"
  echo "Se retiró la contraseña anterior de administrador del archivo local después de la rotación."
}

ensure_credentials_env() {
  local cred_file="$INCENTIVOS_DIR/credentials.env"
  if [[ ! -f "$cred_file" ]]; then
    echo "==> No existe $cred_file; se crea vacío (completarlo si el workflow requiere credenciales de Discord/Sheets)."
    touch "$cred_file"
  fi
}

compose_base() {
  local args=(-f "$COMPOSE_FILE")
  [[ -f "$KEYCLOAK_ENV" ]] && args+=(--env-file "$KEYCLOAK_ENV")
  docker compose "${args[@]}" "$@"
}

compose_auth() {
  docker compose --env-file "$KEYCLOAK_ENV" -f "$COMPOSE_FILE" --profile auth "$@"
}

start_keycloak() {
  [[ -f "$KEYCLOAK_ENV" ]] || return 0
  validate_keycloak_env || return 1

  echo "==> Levantando Keycloak..."
  compose_auth up -d keycloak || return 1

  echo "    Esperando a que Keycloak esté sano (hasta 3 minutos)..."
  local health=""
  for _ in $(seq 1 90); do
    health="$(docker inspect --format '{{.State.Health.Status}}' donatrack-keycloak 2>/dev/null || true)"
    [[ "$health" == "healthy" ]] && break
    if [[ "$health" == "unhealthy" ]]; then
      echo "Keycloak quedó unhealthy. Revisá: docker logs donatrack-keycloak --tail 100" >&2
      return 1
    fi
    sleep 2
  done
  if [[ "$health" != "healthy" ]]; then
    echo "Keycloak no quedó healthy a tiempo. Revisá: docker logs donatrack-keycloak --tail 100" >&2
    return 1
  fi

  echo "==> Aplicando configuración del realm donatrack..."
  compose_auth run --rm --no-deps keycloak-config || return 1
  remove_previous_admin_password
}

start_infra() {
  ensure_credentials_env
  echo "==> Levantando infraestructura (RabbitMQ, MySQL, Mongo, Adminer, n8n y Mailpit)..."
  compose_base up -d || return 1

  echo "    Esperando a que n8n registre el webhook (hasta 60s)..."
  for _ in $(seq 1 30); do
    local code
    code="$(curl -s -o /dev/null -w '%{http_code}' -X POST http://localhost:5678/webhook/events/insignia-otorgada 2>/dev/null)"
    [[ "$code" != "404" && "$code" != "000" ]] && break
    sleep 2
  done
}

start_java_service() {
  local name="$1" with_auth="$2" port="${PORTS[$1]}"
  local pid_file="$PID_DIR/$name.pid"

  if [[ -f "$pid_file" ]] && kill -0 "$(cat "$pid_file")" 2>/dev/null; then
    echo "==> $name ya está corriendo (PID $(cat "$pid_file"), puerto $port)"
    return 0
  fi

  local jar
  jar="$(find_jar "$name")"
  if [[ -z "$jar" ]]; then
    echo "!! No se encontró el jar de $name. Corré '$0 build' primero." >&2
    return 1
  fi

  echo "==> Iniciando $name en el puerto $port..."
  (
    load_env_file "$ROOT_DIR/$name/.env"
    if [[ "$with_auth" == "1" ]]; then
      load_env_file "$KEYCLOAK_ENV"
      if [[ -z "${SEGURIDAD_HABILITADA:-}" ]]; then
        export SEGURIDAD_HABILITADA=true
      fi
      case "$name" in
        servicio-web) [[ -n "${KEYCLOAK_WEB_CLIENT_SECRET:-}" ]] && export KEYCLOAK_CLIENT_SECRET="$KEYCLOAK_WEB_CLIENT_SECRET" ;;
        servicio-donaciones) [[ -n "${KEYCLOAK_DONACIONES_CLIENT_SECRET:-}" ]] && export KEYCLOAK_CLIENT_SECRET="$KEYCLOAK_DONACIONES_CLIENT_SECRET" ;;
        servicio-incentivos) [[ -n "${KEYCLOAK_INCENTIVOS_CLIENT_SECRET:-}" ]] && export KEYCLOAK_CLIENT_SECRET="$KEYCLOAK_INCENTIVOS_CLIENT_SECRET" ;;
        *) unset KEYCLOAK_CLIENT_SECRET ;;
      esac
      # No entregar al proceso Java las credenciales de administrador, MySQL ni
      # Google que solo necesita Compose para configurar Keycloak.
      unset KEYCLOAK_ADMIN_USERNAME KEYCLOAK_ADMIN_PASSWORD KEYCLOAK_ADMIN_PASSWORD_PREVIOUS
      unset KEYCLOAK_DB_PASSWORD KEYCLOAK_DB_ROOT_PASSWORD
      unset KEYCLOAK_WEB_CLIENT_SECRET KEYCLOAK_DONACIONES_CLIENT_SECRET KEYCLOAK_INCENTIVOS_CLIENT_SECRET
      unset GOOGLE_CLIENT_ID GOOGLE_CLIENT_SECRET
    else
      export SEGURIDAD_HABILITADA=false
      unset KEYCLOAK_CLIENT_SECRET
    fi
    exec nohup java -jar "$jar" --server.port="$port" > "$LOG_DIR/$name.log" 2>&1
  ) &
  echo $! > "$pid_file"
}

stop_java_service() {
  local name="$1" pid_file="$PID_DIR/$1.pid"
  [[ -f "$pid_file" ]] || return 0
  local pid
  pid="$(cat "$pid_file")"
  if kill -0 "$pid" 2>/dev/null; then
    echo "==> Deteniendo $name (PID $pid)..."
    kill "$pid"
  fi
  rm -f "$pid_file"
}

stop_infra() {
  echo "==> Deteniendo infraestructura (incluido Keycloak si está arriba)..."
  compose_base --profile auth down
}

up() {
  local requested="$1" with_auth=0 missing=0
  if [[ "$requested" == "--sin-keycloak" ]]; then
    echo "==> Modo sin Keycloak; se desactiva la seguridad de las APIs para este arranque."
  elif [[ -f "$KEYCLOAK_ENV" ]]; then
    with_auth=1
  fi

  for name in $(implemented_services); do
    [[ -n "$(find_jar "$name")" ]] || missing=1
  done
  if [[ "$missing" -eq 1 ]]; then
    build || echo "==> Sigo con lo que sí compiló."
  fi

  start_infra || return 1
  if [[ "$with_auth" == "1" ]]; then
    start_keycloak || return 1
  elif [[ "$requested" != "--sin-keycloak" ]]; then
    echo "==> No se encontró keycloak/.env; se levantará el sistema sin autenticación."
  fi

  for name in $(implemented_services); do
    start_java_service "$name" "$with_auth" || return 1
  done

  echo ""
  status
  echo ""
  echo "Cliente web: http://localhost:${PORTS[servicio-web]}"
}

down() {
  for name in $(implemented_services); do
    stop_java_service "$name"
  done
  stop_infra
}

status() {
  echo "Servicios:"
  for name in "${SERVICE_ORDER[@]}"; do
    if ! is_implemented "$name"; then
      echo "  [N/A]  $name (sin código todavía)"
      continue
    fi
    local pid_file="$PID_DIR/$name.pid"
    if [[ -f "$pid_file" ]] && kill -0 "$(cat "$pid_file")" 2>/dev/null; then
      echo "  [UP]   $name (PID $(cat "$pid_file"), puerto ${PORTS[$name]})"
    else
      echo "  [DOWN] $name"
    fi
  done
  echo ""
  echo "Infraestructura:"
  local running
  running="$(compose_base --profile auth ps --status running --format '{{.Service}}' 2>/dev/null)"
  for infra in rabbitmq mysql mongo adminer n8n mailpit keycloak; do
    if echo "$running" | grep -qx "$infra"; then
      echo "  [UP]   $infra"
    else
      echo "  [DOWN] $infra"
    fi
  done
}

logs() {
  local name="${1:-}"
  if [[ -z "$name" ]]; then
    echo "Uso: $0 logs <servicio>" >&2
    exit 1
  fi
  case "$name" in
    n8n|mysql|rabbitmq|mongo|adminer|mailpit|keycloak)
      compose_base --profile auth logs -f "$name"
      ;;
    *) tail -f "$LOG_DIR/$name.log" ;;
  esac
}

cmd="${1:-up}"
[[ $# -gt 0 ]] && shift
case "$cmd" in
  build) build ;;
  up)
    if [[ $# -gt 1 || ( $# -eq 1 && "$1" != "--sin-keycloak" ) ]]; then usage; exit 1; fi
    up "${1:-}"
    ;;
  down) down ;;
  status) status ;;
  logs) logs "$@" ;;
  -h|--help) usage ;;
  *) usage; exit 1 ;;
esac
