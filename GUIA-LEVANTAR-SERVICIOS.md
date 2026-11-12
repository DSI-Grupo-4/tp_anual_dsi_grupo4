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

Esta configuración deja funcionando el flujo: `http://localhost:8085/admin` redirige a Keycloak, el usuario entra, vuelve a `servicio-web` con sesión y "Cerrar sesión" cierra también la sesión de Keycloak. No incluye Google ni la protección de las APIs.

El contenedor que ya está corriendo no ve el tema hasta recrearlo. Este comando no borra volúmenes:

```powershell
docker compose --env-file keycloak/.env --profile auth -f docker-compose.integration.yml up -d keycloak
```

En la consola (http://localhost:8180, realm `master`, y después cambiar a `donatrack`):

1. **Realm settings → Themes.** Login theme: `donatrack`. Save.
2. **Realm settings → Localization.** Internationalization ON, supported locale `es`, default locale `es`. Save. El registro de usuarios queda desactivado.
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

El botón "Crear cuenta" sigue yendo a `/ingresar`, que ahora inicia el login. El registro en Keycloak queda para la fase siguiente.

En una instalación nueva el JSON ya crea el cliente `servicio-web`, sin secret. Keycloak genera uno al importar el realm: igual hay que copiarlo de Credentials. Si ese import fallara, no borres la base; creá el cliente en la consola con los mismos datos de arriba.

Para bajarlo sin tocar el resto de la infraestructura:

```powershell
docker compose --profile auth -f docker-compose.integration.yml stop keycloak
```

`stop-servicios` también lo baja junto con MySQL, para que no quede corriendo sin su base. Los datos quedan en `mysql-data`.

Las versiones anteriores de esta configuración usaban un volumen `keycloak-data` con H2. Si existe en tu máquina, ya no se usa y no hace falta borrarlo.