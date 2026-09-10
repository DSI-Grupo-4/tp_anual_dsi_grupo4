---
name: donatrack-api-docs-bruno
description: Documentación y pruebas de los endpoints de DonaTrack con Bruno, sincronizado contra el spec OpenAPI de cada servicio Spring Boot. Usar al crear o modificar cualquier endpoint (para mantener la colección al día), al configurar Bruno por primera vez en un servicio, al generar el sitio de documentación estática, o cuando el usuario pida probar/documentar la API en vez de usar Postman.
---

# Documentación y pruebas de API con Bruno — DonaTrack

Reemplaza a Postman como herramienta de referencia de API y de pruebas manuales endpoint por endpoint. Decisión tomada por costo (gratis, sin cuenta) y por mantenibilidad (sync automático contra el spec real, no colecciones armadas a mano que se desactualizan).

## Setup inicial por servicio (una vez)
1. Cada servicio Spring Boot ya expone su spec OpenAPI vía `springdoc-openapi-starter-webmvc-ui` en `/v3/api-docs` (ver `donatrack-docker-deploy`). Ese spec es la fuente de verdad.
2. Crear la colección con el CLI, una carpeta por servicio versionada en el repo:
   ```
   bru import openapi \
     --source http://localhost:<puerto>/v3/api-docs \
     --output bruno/<nombre-servicio> \
     --collection-name "DonaTrack - <Servicio>" \
     --collection-format bru
   ```
3. Repetir para Donaciones, Logística, Incentivos y Notificaciones — cuatro colecciones independientes dentro de `bruno/`, no una colección monolítica, para que cada equipo/servicio pueda iterar sin pisarse.
4. Configurar entornos (`environments/`) por colección: `local` (localhost + puertos de docker-compose) y `deploy` (URL pública del servicio, hoy solo aplica a Logística que está desplegado).
5. Habilitar **OpenAPI Sync** en cada colección (contexto de colección → OpenAPI Sync → conectar contra la misma URL de `/v3/api-docs`) para que Bruno detecte cambios de spec automáticamente cada vez que se abre.
6. Commitear `bruno/` al repo. Las colecciones son archivos `.bru` en texto plano: se revisan en PR como cualquier otro cambio de código.

## Mantenerla actualizada — esto es un paso del workflow, no un evento único
Cada vez que se agrega, modifica o borra un endpoint (lo cual va a pasar constantemente durante el audit-and-fix del repo existente, ver `donatrack-audit-and-fix-workflow`), el ciclo de trabajo incluye:
1. Levantar el servicio localmente (o correr contra el spec ya generado si está corriendo en compose).
2. Re-sincronizar la colección afectada:
   ```
   bru import openapi --source http://localhost:<puerto>/v3/api-docs --output bruno/<servicio> --overwrite
   ```
   `--overwrite` regenera método/URL/estructura desde el spec pero respeta scripts y tests escritos a mano en Bruno (asserts, variables de entorno) — no hace falta rehacer las pruebas manuales cada vez.
3. Si aparecen endpoints huérfanos (removidos del spec pero con contenido manual agregado), Bruno los deja marcados en vez de borrarlos silenciosamente — revisar y decidir si se borran o si el endpoint todavía existe pero falta documentarlo en el código (`@Operation`, `@ApiResponse` de Springdoc).
4. Este re-sync **es un paso obligatorio dentro del ciclo síntoma→propuesta→aprobación→plan** de `donatrack-audit-and-fix-workflow`: cuando el plan de implementación incluye tocar un endpoint, el plan debe incluir explícitamente "re-sincronizar colección Bruno de `<servicio>`" como uno de los pasos, no como un after-thought.

## Pruebas endpoint por endpoint
- Cada request en Bruno debe tener al menos un test básico (`expect(res.status).to.equal(200)` o el código esperado, y validación mínima del body) usando la sintaxis de test de Bruno — no dejar requests "vacíos" que solo sirven para pegarle a la API sin verificar nada.
- Priorizar casos con los ejemplos concretos del propio enunciado de la cátedra (donación de Arcos Plateados, comedor Escobar Sonrisas, escuela rural N°10, CSV de Ana Pérez / Arcos Plateados S.A.) para que las pruebas tengan sentido de dominio y sean reconocibles al mostrarlas en la defensa.
- Para flujos que dependen de estado (ej. transición de una donación por toda la máquina de estados), usar variables de colección/entorno de Bruno para encadenar requests (guardar el id devuelto por un POST y reusarlo en el siguiente request), en vez de hardcodear ids.

## Documentación publicable (cubre el entregable de "documentar en Swagger/Postman o similar")
- Generar el sitio estático desde cada colección: `Collection Settings → Documentation → Generate Docs` (o el comando CLI equivalente), lo cual produce un HTML autocontenido por servicio.
- Publicar esos HTML (ej. GitHub Pages, o servidos como estáticos junto al front) como referencia de API para el equipo y para la cátedra — no reemplaza Swagger UI en runtime (que sigue siendo útil para explorar en vivo), lo complementa como snapshot versionado.

## Qué NO hacer
- No mantener colecciones de Bruno editadas 100% a mano sin conectarlas al spec — se pierde exactamente la ventaja por la que se eligió Bruno.
- No compartir credenciales reales (API keys, passwords) en archivos `.bru` versionados — usar variables de entorno de Bruno marcadas como secretas y un `.env`/vault local no versionado.
- No crear una sola colección gigante para los cuatro servicios — rompe el paralelismo entre equipos y complica el OpenAPI Sync (cada servicio tiene su propio spec).
