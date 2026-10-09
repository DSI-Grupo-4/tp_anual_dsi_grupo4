# Guía para levantar DonaTrack

## Requisitos

- Java 17 o superior.
- Maven disponible desde la terminal (`mvn -version`).
- Docker Desktop corriendo (para RabbitMQ, MySQL y n8n).
- Puertos 8080, 8081, 8082, 8083 (servicios), 3306 (MySQL), 5672/15672 (RabbitMQ) y 5678 (n8n) libres.

Ejecutar los comandos desde la raíz del repositorio:

```powershell
cd "A:\Diseño\tp_anual_dsi_grupo4"
```

## Levantar todos los servicios con un comando

En PowerShell:

```powershell
.\run-servicios.ps1
```

El script levanta primero la infraestructura de Docker (RabbitMQ, MySQL y n8n, definida en `docker-compose.integration.yml`), compila el proyecto sin ejecutar las pruebas y levanta los cuatro servicios en segundo plano. Los logs y los identificadores de proceso quedan en la carpeta `.servicios`. Requiere Docker Desktop corriendo.

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

`stop-servicios.ps1` también baja la infraestructura de Docker (RabbitMQ, MySQL, n8n) cuando se detienen los 4 servicios (sin `-Service`). Si se detiene solo uno puntual, la infraestructura queda arriba para no afectar al resto.

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

## Puertos y Swagger

| Servicio | Puerto | Swagger |
| --- | ---: | --- |
| Donaciones | 8080 | http://localhost:8080/swagger-ui.html |
| Incentivos | 8081 | http://localhost:8081/swagger-ui/index.html |
| Notificaciones | 8082 | http://localhost:8082/swagger-ui/index.html |
| Logística | 8083 | http://localhost:8083/swagger-ui/index.html |

Si `/swagger-ui.html` redirige, también se puede probar `/swagger-ui/index.html` en el mismo puerto.

## Verificar que arrancaron

Además de Swagger, se puede comprobar que cada puerto está escuchando:

```powershell
Test-NetConnection localhost -Port 8080
Test-NetConnection localhost -Port 8081
Test-NetConnection localhost -Port 8082
Test-NetConnection localhost -Port 8083
```

La primera ejecución puede tardar porque Maven necesita descargar dependencias y compilar los módulos.

## Infraestructura (RabbitMQ, MySQL, n8n)

`run-servicios.ps1`/`run-servicios.sh` ya la levantan solos. Para manejarla aparte (por ejemplo, para levantar solo la infraestructura sin los servicios Java):

```powershell
docker compose -f docker-compose.integration.yml up -d
docker compose -f docker-compose.integration.yml down
```

- n8n (workflow de difusión de insignias de Incentivos): http://localhost:5678
- RabbitMQ (management UI): http://localhost:15672
- MySQL (persistencia de Logística): `localhost:3306`, usuario `root`, password `BasededatosTP1`, base `logistica`.

El esquema de Logística (`servicio-logistica/src/main/resources/db/logistica.sql`) se monta en `docker-entrypoint-initdb.d/` y corre solo la primera vez que se crea el volumen de MySQL. Hibernate arranca con `ddl-auto=validate`: no crea ni modifica tablas, solo valida que las entidades coincidan con lo que generó el script.
