# Guía para levantar DonaTrack

## Requisitos

- Java 17 o superior.
- Maven disponible desde la terminal (`mvn -version`).
- Docker Desktop corriendo (para RabbitMQ, MySQL, Adminer, n8n y, si se va a probar autenticación, Keycloak).
- Puertos 8080, 8081, 8082, 8083 (servicios), 8085 (cliente web), 3306 (MySQL), 5672/15672 (RabbitMQ), 8090 (Adminer), 5678 (n8n), 1025/8025 (Mailpit) y 8180 (Keycloak) libres.

Ejecutar los comandos desde la raíz del repositorio:

```powershell
cd "A:\Diseño\tp_anual_dsi_grupo4"
```

## Levantar todos los servicios con un comando

En PowerShell:

```powershell
.\run-servicios.ps1
```

El script levanta primero la infraestructura de Docker (RabbitMQ, MySQL, Adminer y n8n, definida en `docker-compose.integration.yml`), compila el proyecto sin ejecutar las pruebas y levanta los cuatro servicios y el cliente web (`servicio-web`) en segundo plano. El cliente web arranca último porque consume las APIs de los demás. En Linux o macOS, el equivalente es `./run-servicios.sh up`. Los logs y los identificadores de proceso quedan en la carpeta `.servicios`. Requiere Docker Desktop corriendo.

Si el proyecto ya está compilado y se quiere omitir ese paso:

```powershell
.\run-servicios.ps1 -SkipBuild
```

Para comprobar el estado de los procesos iniciados:

```powershell
Get-Content .\.servicios\*.pid
Get-Content .\.servicios\*.out.log -Tail 30
Get-Content .\.servicios\*.err.log -Tail 30
```

## Detener y reiniciar los servicios

Si se iniciaron con `run-servicios.ps1`, detener los cuatro desde PowerShell:

```powershell
.\stop-servicios.ps1
```

También se puede detener solo uno:

```powershell
.\stop-servicios.ps1 -Service donaciones
```

El script verifica los PID registrados y termina el árbol de procesos de Maven, incluyendo Java, para liberar los puertos. Conserva los logs. En Windows utiliza `taskkill /T /F`: es una terminación forzada, por lo que conviene ejecutarlo cuando no haya importaciones o solicitudes en curso. Para revisar qué procesos cerraría sin detenerlos, usar `-WhatIf`.

Para aplicar cambios de código, detener los servicios y volver a compilar e iniciar:

```powershell
.\stop-servicios.ps1
.\run-servicios.ps1
```

Cerrar la terminal que ejecutó `run-servicios.ps1` no detiene los servicios en segundo plano. Si se iniciaron manualmente en terminales separadas con `spring-boot:run`, usar **Ctrl+C** en cada terminal para solicitar el cierre normal de Spring.

`stop-servicios.ps1` también baja la infraestructura de Docker (RabbitMQ, MySQL, Adminer, n8n y Keycloak si estaba levantado) cuando se detienen todos los servicios (sin `-Service`). No borra volúmenes. El cliente web se detiene con `-Service web`. Si se detiene solo uno puntual, la infraestructura queda arriba para no afectar al resto.

## Levantar los servicios manualmente

Abrir una terminal para cada comando:

```powershell
mvn -f .\servicio-donaciones\pom.xml spring-boot:run
```

```powershell
mvn -f .\servicio-incentivos\pom.xml spring-boot:run
```

```powershell
mvn -f .\servicio-notificaciones\pom.xml spring-boot:run
```

```powershell
mvn -f .\servicio-logistica\pom.xml spring-boot:run
```

```powershell
# Cliente web (Entrega 6). Con el perfil dev se habilita el catálogo de componentes
# en /dev/componentes y el mapa público muestra datos de ejemplo.
mvn -f .\servicio-web\pom.xml spring-boot:run "-Dspring-boot.run.profiles=dev"
```

## Puertos y Swagger

| Servicio | Puerto | Swagger |
| --- | ---: | --- |
| Donaciones | 8080 | http://localhost:8080/swagger-ui.html |
| Incentivos | 8081 | http://localhost:8081/swagger-ui/index.html |
| Notificaciones | 8082 | http://localhost:8082/swagger-ui/index.html |
| Logística | 8083 | http://localhost:8083/swagger-ui/index.html |
| Cliente web | 8085 | No expone API: se abre en http://localhost:8085 (ver [servicio-web/README.md](servicio-web/README.md)) |

Si `/swagger-ui.html` redirige, también se puede probar `/swagger-ui/index.html` en el mismo puerto.

## Verificar que arrancaron

Además de Swagger, se puede comprobar que cada puerto está escuchando:

```powershell
Test-NetConnection localhost -Port 8080
Test-NetConnection localhost -Port 8081
Test-NetConnection localhost -Port 8082
Test-NetConnection localhost -Port 8083
Test-NetConnection localhost -Port 8085
```

La primera ejecución puede tardar porque Maven necesita descargar dependencias y compilar los módulos.

## Infraestructura (RabbitMQ, MySQL, Adminer, n8n)

`run-servicios.ps1`/`run-servicios.sh` ya la levantan solos. Para manejarla aparte (por ejemplo, para levantar solo la infraestructura sin los servicios Java):

```powershell
docker compose -f docker-compose.integration.yml up -d
docker compose -f docker-compose.integration.yml down
```

- n8n (workflow de difusión de insignias de Incentivos): http://localhost:5678
- RabbitMQ (management UI): http://localhost:15672
- Adminer (consola de administración de la base): http://localhost:8090 -- sistema `MySQL`, servidor `mysql`, usuario `root`, password `BasededatosTP1`, base `logistica`, `incentivos`, `notificaciones` o `donaciones`.
- Mailpit (bandeja de prueba de los emails que envía Notificaciones): http://localhost:8025
- MySQL: `localhost:3306`, usuario `root`, password `BasededatosTP1`, un esquema por servicio (`logistica`, `incentivos`, `notificaciones`, `donaciones`).

Los esquemas de Logística, Incentivos y Donaciones (`servicio-logistica/src/main/resources/db/logistica.sql`, `servicio-incentivos/src/main/resources/db/incentivos.sql`, `servicio-donaciones/src/main/resources/db/donaciones.sql`) se montan en `docker-entrypoint-initdb.d/` y corren solo la primera vez que se crea el volumen de MySQL. Hibernate arranca con `ddl-auto=validate`: no crea ni modifica tablas, solo valida que las entidades coincidan con lo que generaron los scripts.

El de Notificaciones es `servicio-notificaciones/src/main/resources/db/notificaciones.sql`. Si tu volumen de MySQL ya existía antes de que se agregara, ejecutalo una vez a mano desde Adminer (SQL command) o recreá el volumen con `docker compose -f docker-compose.integration.yml down -v` (borra todos los datos).

## Keycloak (autenticación local)

`run-servicios.ps1` y `run-servicios.sh` no levantan Keycloak. El servicio está en `docker-compose.integration.yml` con el perfil `auth`, así que un `docker compose up -d` de la infraestructura actual no lo arranca ni lo recrea.

Keycloak guarda sus datos en una base `keycloak` del mismo MySQL del proyecto, con un usuario `keycloak` que solo tiene permisos sobre esa base. No toca `donaciones`, `incentivos` ni `notificaciones`. La base y el usuario los crea el contenedor `keycloak-db-init`, que corre antes de Keycloak en cada arranque y termina. Es idempotente: funciona igual con un volumen de MySQL nuevo o con uno que ya tiene datos.

La primera vez, en la raíz del repo:

```powershell
Copy-Item keycloak\.env.example keycloak\.env
```

Editar `keycloak\.env` y reemplazar los tres valores `definir-...` por contraseñas propias. `KEYCLOAK_DB_PASSWORD` no puede tener comillas simples ni barras invertidas. `KEYCLOAK_DB_ROOT_PASSWORD` tiene que ser la misma clave de root que usa el servicio `mysql` del compose: la necesita el inicializador para crear la base y el usuario. Ese archivo no se commitea.

```powershell
docker compose --env-file keycloak/.env --profile auth -f docker-compose.integration.yml up -d keycloak
```

Si MySQL no estaba corriendo, ese comando lo levanta también, sin recrear su volumen. El primer arranque descarga la imagen `quay.io/keycloak/keycloak:26.8.2` y puede tardar un par de minutos. La consola queda en http://localhost:8180. Se entra con el usuario y la contraseña de `keycloak/.env`, en el realm `master`.

Si Keycloak no arranca, revisar primero el inicializador:

```powershell
docker logs donatrack-keycloak-db-init
docker logs donatrack-keycloak --tail 100
```

Para ver el realm de la aplicación: selector de realm, arriba a la izquierda, `donatrack`. En Realm roles tienen que aparecer `ADMIN`, `DONANTE` y `BENEFICIARIA`.

Comprobación sin abrir el navegador, cuando el contenedor ya responde:

```powershell
Invoke-RestMethod http://localhost:8180/realms/donatrack/.well-known/openid-configuration
```

El `issuer` tiene que ser `http://localhost:8180/realms/donatrack`.

El realm se importa desde `keycloak/import/donatrack-realm.json` solo si todavía no existe en la base `keycloak`. Si se cambia ese archivo y la base ya tiene el realm, Keycloak no lo vuelve a importar. No hay que borrar el realm ni la base: el cliente, el tema y los usuarios de prueba se cargan desde la consola, como está más abajo.

## Login del cliente web (realm que ya existe)

Esta configuración deja funcionando el flujo: `http://localhost:8085/admin` redirige a Keycloak, el usuario entra, vuelve a `servicio-web` con sesión y "Cerrar sesión" cierra también la sesión de Keycloak.

Desde D-045 estos pasos los hace solo el contenedor `keycloak-config` (ver "Configuración automática del realm" más abajo). Se dejan como referencia y para revisar a mano lo que aplicó el script.

El contenedor que ya está corriendo no ve el tema hasta recrearlo. Este comando no borra volúmenes:

```powershell
docker compose --env-file keycloak/.env --profile auth -f docker-compose.integration.yml up -d keycloak
```

En la consola (http://localhost:8180, realm `master`, y después cambiar a `donatrack`):

1. **Realm settings → Themes.** Login theme: `donatrack`. Save.
2. **Realm settings → Localization.** Internationalization ON, supported locale `es`, default locale `es`. Save.
3. **Clients → Create client.**
   - Client type: OpenID Connect. Client ID: `servicio-web`.
   - Client authentication: On. Standard flow: On. Direct access grants: Off.
   - Root URL: `http://localhost:8085`.
   - Valid redirect URIs: `http://localhost:8085/login/oauth2/code/keycloak`.
   - Valid post logout redirect URIs: `http://localhost:8085`.
   - Web origins: `http://localhost:8085`.
   - Save.
4. **Client scopes.** El cliente tiene que tener, como default, `roles`, `profile` y `email`. En Client scopes → `roles` → mapper `realm roles`, "Add to ID token" tiene que estar en On. Sin eso el usuario entra pero las zonas privadas responden 403.
5. **Clients → servicio-web → Credentials.** Copiar el client secret. No va a Git.
6. **Users → Add user**, uno por rol, con contraseña no temporal (Credentials → Temporary Off) y el rol de realm en Role mapping:
   - uno con `ADMIN`, otro con `DONANTE` y otro con `BENEFICIARIA`.

En el repo, copiar el ejemplo y pegar ahí el secret (ese archivo no se versiona):

```powershell
Copy-Item servicio-web\.env.example servicio-web\.env
```

`run-servicios.ps1` lee `servicio-web\.env` antes de levantar el cliente web. Si se arranca a mano, en esa misma terminal:

```powershell
$env:KEYCLOAK_CLIENT_SECRET = "<el secret de la consola>"
mvn -pl servicio-web spring-boot:run
```

Usar siempre `localhost`, no `127.0.0.1`: el redirect y la cookie tienen que coincidir. La cookie de sesión se llama `DONATRACK_SESSION` para que Swagger, en el puerto 8080, no la pise.

Prueba:

1. Abrir http://localhost:8085/admin. Tiene que ir a Keycloak, con el tema de DonaTrack, no con la pantalla violeta por defecto.
2. Entrar con el usuario ADMIN. Vuelve a `/admin` y el encabezado muestra su nombre. Si Donaciones está apagado, el panel avisa que el servicio no está disponible: el login igual funcionó. http://localhost:8085/admin/camiones no llama a las APIs y alcanza para ver la sesión.
3. Abrir http://localhost:8085/donante con ese usuario. Tiene que responder 403.
4. "Cerrar sesión". Vuelve a http://localhost:8085. Entrar de nuevo a `/admin` tiene que pedir usuario otra vez. Si entra solo, la sesión de Keycloak no se cerró: revisar el post logout redirect del cliente.

En una instalación nueva el JSON ya crea el cliente `servicio-web`, sin secret. Keycloak genera uno al importar el realm: igual hay que copiarlo de Credentials. Si ese import fallara, no borres la base; creá el cliente en la consola con los mismos datos de arriba.

Para bajarlo sin tocar el resto de la infraestructura:

```powershell
docker compose --profile auth -f docker-compose.integration.yml stop keycloak
```

`stop-servicios` también lo baja junto con MySQL, para que no quede corriendo sin su base. Los datos quedan en `mysql-data`.

Las versiones anteriores de esta configuración usaban un volumen `keycloak-data` con H2. Si existe en tu máquina, ya no se usa y no hace falta borrarlo.

## Configuración automática del realm (keycloak-config)

`keycloak/config/configurar-realm.sh` deja el realm `donatrack` igual en todas las máquinas, aunque ya exista en la base. Corre en el contenedor `keycloak-config` (perfil `auth`) cuando Keycloak está sano, y termina. No borra el realm, ni usuarios, ni clientes. Se puede correr las veces que haga falta.

Qué configura:

- Tema `donatrack`, idioma español, registro de usuarios con el email como usuario, verificación de email y "¿Olvidaste tu contraseña?". Los mails de Keycloak salen por Mailpit (`mailpit:1025`); se ven en http://localhost:8025.
- Cliente `servicio-web` (login, logout, cuenta de servicio para la landing) con mappers de `realm_access.roles`, `donante_id` y `entidad_ids` en el ID token y el access token.
- Clientes `servicio-donaciones` y `servicio-incentivos`, solo con cuenta de servicio. La de Donaciones recibe `manage-users`, `view-users`, `query-users` y `view-realm` de `realm-management`: los usa para crear y completar cuentas de donantes y representantes.
- Atributos de usuario no declarados en modo `ADMIN_EDIT`: el usuario no ve ni edita `donante_id` ni `entidad_ids`.
- Google, si están `GOOGLE_CLIENT_ID` y `GOOGLE_CLIENT_SECRET`.

Antes de correrlo, completar en `keycloak\.env` los secrets de los clientes (`KEYCLOAK_WEB_CLIENT_SECRET`, `KEYCLOAK_DONACIONES_CLIENT_SECRET`, `KEYCLOAK_INCENTIVOS_CLIENT_SECRET`). Cada integrante elige los suyos: mínimo 16 caracteres entre letras, números y `. _ ~ -`. Si quedan vacíos, el cliente conserva el secret que ya tenía y hay que copiarlo de la consola (Clients → cliente → Credentials).

```powershell
docker compose --env-file keycloak/.env --profile auth -f docker-compose.integration.yml up -d keycloak keycloak-config
docker logs donatrack-keycloak-config
```

El log termina en `Realm donatrack configurado.` Nunca muestra los secrets. También levanta Mailpit si no estaba.

Después, cada servicio usa el secret de su cliente en su propio `.env` (no se versiona; `run-servicios` lo carga):

| Archivo | `KEYCLOAK_CLIENT_SECRET` igual a |
|---|---|
| `servicio-web\.env` | `KEYCLOAK_WEB_CLIENT_SECRET` |
| `servicio-donaciones\.env` | `KEYCLOAK_DONACIONES_CLIENT_SECRET` |
| `servicio-incentivos\.env` | `KEYCLOAK_INCENTIVOS_CLIENT_SECRET` |

Hay un `.env.example` en cada una de esas carpetas.

## Ingreso con Google

En Google Cloud Console (https://console.cloud.google.com), con una cuenta propia; cada integrante puede usar su propio proyecto:

1. Crear un proyecto (o elegir uno) y entrar a **APIs y servicios → Pantalla de consentimiento de OAuth**. Tipo de usuario: Externo. Nombre de la app: DonaTrack. Email de asistencia: el propio. Ámbitos: `openid`, `email` y `profile`. Mientras la app esté "En prueba", agregar como usuarios de prueba las cuentas de Google con las que se va a probar.
2. **APIs y servicios → Credenciales → Crear credenciales → ID de cliente de OAuth.** Tipo: Aplicación web.
   - Orígenes autorizados de JavaScript: `http://localhost:8180`.
   - URI de redireccionamiento autorizados: `http://localhost:8180/realms/donatrack/broker/google/endpoint`. Tiene que ser exactamente esa: apunta a Keycloak, no a `servicio-web`.
3. Copiar el ID de cliente y el secreto a `keycloak\.env` como `GOOGLE_CLIENT_ID` y `GOOGLE_CLIENT_SECRET`. No van a Git ni a ningún otro archivo.
4. Volver a correr `keycloak-config` (comando de arriba). El log dice `Google configurado como proveedor de identidad.`

En Keycloak queda como Identity provider `google` con "Trust email" activado: Google ya verificó ese correo. Si existe una cuenta de Keycloak con el mismo email (por ejemplo, la que creó Donaciones para un donante), Keycloak no la une sola: pide entrar con la contraseña de esa cuenta o confirmar por un mail a ese correo.

La pantalla de login muestra el botón de Google debajo del formulario, con el estilo de botones sociales del tema `donatrack`.

## Seguridad de las APIs

Los cuatro servicios validan el access token de Keycloak (firma, vencimiento e issuer `http://localhost:8180/realms/donatrack`) solo con `SEGURIDAD_HABILITADA=true`. Por defecto es `false` y todo queda abierto como antes: sirve para desarrollar sin Keycloak, para los tests y para el despliegue de Logística en Render. Para probar la seguridad, poner `SEGURIDAD_HABILITADA=true` en el `.env` de **todos** los servicios y de `servicio-web`: un servicio protegido rechaza las llamadas de otro que no manda token.

Quedan públicos sin token: Swagger, `/actuator/health`, `/ping` y `/api/publico/**` de Donaciones; `GET /api/ranking/**` de Incentivos (lo consulta n8n); el link de seguimiento `GET /api/rutas/{id}/seguimiento` y el callback del planificador de Logística. Las reglas por endpoint están en `SeguridadApiConfig` de cada servicio (D-045).

Las tareas automáticas siguen funcionando con un token propio (client_credentials): Donaciones → Logística (lotes y eventos) y Donaciones → Incentivos (perfil y actividad); Incentivos → Donaciones (consulta de un donante). El proveedor alternativo de Logística, que no es nuestro, nunca recibe el token.

Swagger con la seguridad habilitada: las operaciones protegidas responden 401 porque Swagger no pide token. Para probarlas, usar la web o deshabilitar la seguridad.

## Cuentas de donantes y entidades

Con la seguridad habilitada, cada alta o edición de un donante o una entidad (desde la administración o la importación CSV) encola el registro en `.datos/donaciones/cuentas-pendientes`. Cada 10 segundos Donaciones procesa hasta 200:

- por cada email cargado (del donante, de la entidad o de sus representantes) busca la cuenta en Keycloak;
- si no existe, la crea y Keycloak manda el mail "Actualizá tu contraseña" con el link para el primer acceso (válido 7 días; después sirve "¿Olvidaste tu contraseña?");
- si existe y su email está verificado, le agrega `donante_id` o `entidad_ids` y el rol `DONANTE` o `BENEFICIARIA`; si el email no está verificado, no la toca;
- los emails que ya no figuran pierden ese vínculo.

Nunca se asigna `ADMIN`: las cuentas administradoras se crean a mano en la consola. Para los donantes y entidades cargados antes de habilitar la seguridad, un ADMIN puede llamar a `POST /api/cuentas/sincronizar` de Donaciones.

Quien se registra por su cuenta (o entra con Google) y todavía no tiene rol cae en http://localhost:8085/cuenta. "Buscar mis datos" llama a `POST /api/cuentas/vincular`: si su email verificado figura en un donante o una entidad, se vincula y vuelve a pedir el login para traer el rol nuevo.

Prueba con Docker:

1. `keycloak-config` corrido, los `.env` con los secrets y `SEGURIDAD_HABILITADA=true` en todos. Levantar con `run-servicios`.
2. Entrar como ADMIN y dar de alta un donante con un email inventado. A los pocos segundos llega a http://localhost:8025 el mail de Keycloak. Seguir el link, elegir contraseña e ingresar: tiene que entrar a `/donante`.
3. Reglas de la API. Sin token, `Invoke-WebRequest http://localhost:8080/api/donantes` responde 401. Con el token de servicio de `servicio-web` responde 200, y una operación de administración responde 403:

   ```powershell
   $secret = Read-Host "Secret de servicio-web"
   $t = (Invoke-RestMethod -Method Post http://localhost:8180/realms/donatrack/protocol/openid-connect/token `
         -Body @{ grant_type = "client_credentials"; client_id = "servicio-web"; client_secret = $secret }).access_token
   Invoke-RestMethod http://localhost:8080/api/donantes -Headers @{ Authorization = "Bearer $t" }
   Invoke-WebRequest -Method Delete http://localhost:8080/api/donantes/1 -Headers @{ Authorization = "Bearer $t" }
   ```

   La pertenencia (un DONANTE ve su `/api/donantes/{id}` y recibe 403 en el de otro) necesita un token de usuario; las pantallas de donante y entidad todavía no llaman a la API, así que hoy se prueba con los tests o desde la consola de Keycloak (Clients → servicio-web → Client scopes → Evaluate → Generated access token, para ver `donante_id`).
4. Desde "Crear cuenta" en `/registro`, registrar un email que no figura en ningún donante. Después de verificarlo en Mailpit, el ingreso lleva a `/cuenta` y "Buscar mis datos" avisa que no encontró nada.
5. Con Google configurado, entrar con una cuenta de Google de prueba. La primera vez cae en `/cuenta`.
6. Cerrar sesión y volver a `/admin`: tiene que pedir usuario. En la pestaña de cookies, `DONATRACK_SESSION` ya no está.
7. Sesión vencida: en Keycloak, Users → el usuario → Sessions → Sign out. El access token que ya tiene la web sigue valiendo hasta que vence (5 minutos por defecto); la primera acción que llama a una API después de eso no puede renovarlo y vuelve al login con el aviso "Tu sesión venció". Si se borra la sesión de la web (cookie vieja tras reiniciar `servicio-web`), entrar a una zona privada muestra el mismo aviso.