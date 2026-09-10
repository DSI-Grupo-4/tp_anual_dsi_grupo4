---
name: donatrack-docker-deploy
description: Contenerización con Docker/Docker Compose, documentación de endpoints (Swagger/Postman) y despliegue en la nube del Servicio de Logística de DonaTrack, incluyendo cómo pausarlo para reducir costos. Usar al escribir Dockerfiles, docker-compose.yml, configurar Springdoc/OpenAPI, o al elegir y configurar una plataforma de despliegue gratuita/de bajo costo.
---

# Docker y Despliegue — DonaTrack

## Contenerización (todos los servicios, no solo Logística)
- Un `Dockerfile` por servicio Spring Boot (multi-stage: build con Maven/JDK, runtime con JRE liviano tipo `eclipse-temurin:21-jre-alpine`). Si hoy existe un único `Dockerfile` en la raíz del repo cubriendo el build multimódulo, evaluar en el audit (Fase 0 de `donatrack-audit-and-fix-workflow`) si conviene separarlo en uno por servicio para builds independientes en CI/Render, o si el equipo prefiere mantenerlo unificado — no es un cambio a hacer sin pasar por el ciclo de aprobación.
- **Decisión tomada (ver `decisiones.md` D-001)**: un único `docker-compose.yml` en la raíz del repo, que levanta MySQL8 (con volumen persistente y un único init script que crea los esquemas `donaciones`, `logistica`, `incentivos`, `notificaciones`), RabbitMQ, n8n (ver sección dedicada más abajo) y los 4 servicios Spring Boot, todos en una red compartida. Ya existe `servicio-incentivos/docker-compose.yml` y `servicio-incentivos/credentials.env` a nivel de servicio — al crear el compose raíz, consolidar esa configuración ahí (no mantener dos fuentes de verdad para el mismo servicio) y tratar la migración como una tarea del ciclo síntoma→propuesta→aprobación, no un merge silencioso.
- Variables de entorno para credenciales, nunca hardcodeadas en el Dockerfile ni versionadas en texto plano — si `credentials.env` tiene secretos reales y está commiteado, es un hallazgo de seguridad a resolver en el audit antes de seguir (sacarlo del historial, mover a variables de entorno de Render/CI).
- Healthchecks en compose (`actuator/health` de Spring Boot, `/healthz` de n8n) para que los servicios no arranquen antes de que MySQL/RabbitMQ/n8n estén listos.

## n8n — flujo de difusión de insignias y ranking mensual (Servicio de Incentivos)
La consigna pide explícitamente automatizar con una herramienta *low-code* (ej. n8n) dos flujos del Servicio de Incentivos: (1) publicar en redes sociales cuando una persona donante obtiene una insignia, y (2) el cálculo/publicación del ranking mensual al cierre de mes.
- Imagen oficial `n8nio/n8n` en el `docker-compose.yml`, con volumen persistente (`n8n_data:/home/node/.n8n`) para no perder los workflows configurados al recrear el contenedor.
- El disparador de "insignia obtenida" es responsabilidad del **Servicio de Incentivos**: al completarse una misión, Incentivos llama al **webhook de n8n** (`POST http://n8n:5678/webhook/insignia-obtenida` en la red interna de compose) con el texto descriptivo y los datos de la insignia — no al revés (n8n no debe hacer polling a Incentivos).
- El workflow ya está exportado y versionado en `servicio-incentivos/workflows/servicio-incentivos-difusion.json` — al levantar n8n en el compose raíz, importarlo desde esa ruta (no crear una carpeta `n8n/workflows/` nueva en la raíz, ya existe la convención dentro del módulo del servicio dueño del workflow).
- Esta llamada de Incentivos hacia n8n es un detalle de automatización de contenido, **no** reemplaza la integración asíncrona por cola con el Servicio de Notificaciones (esa sigue siendo obligatoria vía RabbitMQ, ver skill `donatrack-async-notifications`). Son dos integraciones distintas con propósitos distintos: n8n → difusión pública en redes sociales; cola → notificación privada al usuario.
- El ranking mensual puede modelarse como un workflow de n8n con **trigger Cron** (cierre de mes) que llama a un endpoint interno de Incentivos para calcular y persistir el ranking, o como una tarea programada (`@Scheduled`) dentro del propio servicio que solo delega en n8n la parte de publicación/difusión — decidir una de las dos y documentarla; no duplicar la lógica de cálculo en ambos lados.
- Credenciales de la red social (API keys) van como variables de entorno del contenedor de n8n (o en un `.env` no versionado), nunca hardcodeadas dentro del JSON del workflow exportado al repo.
- Ya está resuelto el entregable "flujo automatizado de publicación y difusión de insignias" (Entrega 2): el JSON existe versionado en `servicio-incentivos/workflows/`. Si se agrega el workflow del ranking mensual como pieza separada, versionarlo en esa misma carpeta con nombre descriptivo (ej. `servicio-incentivos-ranking-mensual.json`), no crear una ubicación nueva.

## Documentación de endpoints (exigido desde Entrega 3, sigue vigente)
- **Swagger/OpenAPI**: agregar `springdoc-openapi-starter-webmvc-ui` a cada servicio, exponer `/swagger-ui.html` y `/v3/api-docs`. Documentar DTOs de request/response y códigos de estado, no dejar el Swagger autogenerado sin descripciones — este spec es además la fuente de la que se sincroniza la colección de Bruno (ver `donatrack-api-docs-bruno`), así que su calidad impacta directamente en la documentación de pruebas.
- **Pruebas y colección de referencia**: se usa **Bruno**, no Postman (ver `donatrack-api-docs-bruno` para el detalle completo) — colecciones `.bru` versionadas en el repo, sincronizadas contra `/v3/api-docs` de cada servicio.

## Despliegue del Servicio de Logística
- Objetivo explícito de la consigna: accesible vía web a través de sus URIs, y **puede pausarse** para reducir consumo hasta la defensa.
- Plataformas razonables para un TP sin presupuesto: Render, Railway o Fly.io (planes free/hobby con sleep automático) para el servicio, y una base gestionada o un contenedor propio de MySQL/RabbitMQ según lo que permita el plan elegido. Documentar la URL pública y cómo reactivar el servicio si la plataforma lo duerme.
- Si la plataforma no soporta "pausar" nativamente, dejar documentado el procedimiento manual (`docker compose down` / escalar a 0 instancias) y el procedimiento inverso para el día de la defensa — el entregable es que el despliegue exista y sea reproducible, no que esté corriendo 24/7 gastando cuota.

## Checklist antes de dar por hecho el entregable de despliegue
1. La URL pública responde `GET /actuator/health` (o equivalente) con 200.
2. Swagger accesible en esa misma URL pública, no solo en local.
3. Las variables sensibles (credenciales de DB, de la cola, API keys de redes sociales usadas por n8n) están en variables de entorno del proveedor, no en el repo.
4. Existe un README con los pasos para levantar todo localmente vía `docker compose up` como respaldo si el despliegue en la nube está pausado el día de la revisión.
5. El contenedor de n8n con sus workflows importados aparece en el `docker-compose.yml` y en el diagrama de despliegue actualizado — no es un detalle "aparte" del sistema, es un componente de la arquitectura de Incentivos.
