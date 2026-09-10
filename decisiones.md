# Bitácora de decisiones ante ambigüedad — DonaTrack

<!-- Formato de entrada, ver .claude/skills/donatrack-ambiguity-resolution/SKILL.md -->

## [D-001] Organización de docker-compose para infraestructura y servicios
- **Fecha:** 2026-09-10
- **Servicio(s) afectado(s):** Todos (Donaciones, Logística, Incentivos, Notificaciones) + infraestructura (MySQL, RabbitMQ, n8n)
- **Entrega vigente al momento:** Entrega 4
- **Contexto:** El repo ya tenía un `Dockerfile` único en la raíz y un `docker-compose.yml` + `credentials.env` a nivel de `servicio-incentivos/`. No estaba definido si el compose de Entrega 4 debía ser único a nivel raíz, mantenerse por servicio, o un híbrido (compose raíz solo para infraestructura compartida + compose por servicio).
- **Fuentes revisadas:** decisiones.md (sin entradas previas, es la primera) / entrega4-requerimientos.md (no especifica organización de archivos de compose, solo exige "un único motor de base de datos relacional, con un esquema por servicio") / PDF de consigna, sección Entrega 4 "Requerimiento de Despliegue" (solo exige que Logística sea accesible vía web, no define la organización de compose).
- **Opciones consideradas:**
  - A) Un `docker-compose.yml` único en la raíz que levanta todo el sistema.
  - B) Mantener `docker-compose.yml` por servicio (como ya existía en Incentivos), sin compose raíz.
  - C) Compose raíz solo para infraestructura compartida (MySQL/RabbitMQ/n8n) + cada servicio con el suyo propio.
- **Decisión tomada:** Opción A — un único `docker-compose.yml` en la raíz del repo que levanta todo: MySQL8, RabbitMQ, n8n y los 4 servicios Spring Boot. La configuración existente en `servicio-incentivos/docker-compose.yml` y `servicio-incentivos/credentials.env` debe consolidarse ahí como parte del audit (no queda como fuente de verdad paralela).
- **Definida por:** usuario
