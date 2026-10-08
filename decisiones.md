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

## [D-002] Modelo de categorías/subcategorías de bienes donados
- **Fecha:** 2026-09-10
- **Servicio(s) afectado(s):** Donaciones
- **Entrega vigente al momento:** Entrega 4 (afecta dominio heredado de Entrega 1)
- **Contexto:** Al comparar DDC vs. código vs. consigna para el flujo de carga de donaciones, se detectó que `Subcategoria` en código es un objeto plano (solo `nombre`), sin atributos propios. La consigna (PDF, pág. 12) exige que ciertas subcategorías tengan atributos particulares (usado/nuevo para mobiliario/vestimenta, fecha de vencimiento para perecederos, cantidad en una unidad determinada siempre). El diagrama original (nota de diseño en `diagramas/ddc/DONACIONES.drawio.xml`) apuntaba a que el Depósito gestione categorías/subcategorías dinámicamente, "para poder construir los atributos de cada tipo de ítem sin recompilar". El código tiene además 3 clases huérfanas (`Alimento`, `Mobiliario`, `Vestimenta`) que sugieren el camino alternativo de subclases fijas por tipo, nunca terminado ni conectado.
- **Fuentes revisadas:** decisiones.md (sin match) / entrega4-requerimientos.md (no cubre modelado de dominio de Entrega 1) / PDF consigna sección "Servicio de Donaciones - Donaciones y segmentación" (exige los atributos variables por subcategoría, pero no define el mecanismo de implementación).
- **Opciones consideradas:**
  - A) Subclases fijas por tipo (`Alimento`/`Mobiliario`/`Vestimenta` ya esbozadas).
  - B) Atributos dinámicos gestionados por el Depósito (diseño original del equipo, tipo EAV).
  - C) Híbrido: subclases fijas + flags genéricos reusables (fechaVencimiento/estadoUso opcionales en la clase base).
- **Decisión tomada:** Opción B — atributos dinámicos gestionados por Depósito, fiel al diseño original documentado en la nota del DDC. Las clases huérfanas `Alimento`/`Mobiliario`/`Vestimenta` quedan obsoletas y se eliminan como parte del refactor de dominio.
- **Definida por:** usuario

## [D-003] Estado `PENDIENTE_CONFIRMACION` de la donación
- **Fecha:** 2026-09-10
- **Servicio(s) afectado(s):** Donaciones
- **Entrega vigente al momento:** Entrega 4 (afecta dominio heredado de Entrega 2)
- **Contexto:** El enum `EstadoTrack` declara `PENDIENTE_CONFIRMACION`, pero no aparece en la Figura 2 (máquina de estados oficial) del PDF de consigna, que va directo de "En depósito" a "Asignación realizada" cuando el algoritmo asigna entidad. El estado tampoco tiene ninguna transición válida definida en `Donacion.TRANSICIONES_VALIDAS`, ni se usa como estado inicial — es un literal muerto. El texto de la consigna sí describe una ventana donde el algoritmo propone candidatas y un administrador debe confirmar el destino final antes de asignar, lo cual podría justificar un estado intermedio.
- **Fuentes revisadas:** decisiones.md (sin match) / entrega4-requerimientos.md (no detalla la máquina de estados) / PDF consigna sección "Servicio de Donaciones - Estados de las donaciones" y Figura 2 (7 estados oficiales, sin `PENDIENTE_CONFIRMACION`).
- **Opciones consideradas:**
  - A) Eliminarlo — ajustarse estrictamente a los 7 estados de la Figura 2; la ventana de candidatas sin confirmar se maneja como dato transitorio (campo `candidatas` en `Donacion`), no como estado formal.
  - B) Mantenerlo como 8vo estado real, intermedio entre "En depósito" y "Asignación realizada".
- **Decisión tomada:** Opción A — se elimina el estado `PENDIENTE_CONFIRMACION` del enum `EstadoTrack`. La máquina de estados queda ceñida a los 7 estados oficiales de la Figura 2 del PDF.
- **Definida por:** usuario

## [D-004] Cardinalidad de representantes de `PersonaJuridica`
- **Fecha:** 2026-09-10
- **Servicio(s) afectado(s):** Donaciones
- **Entrega vigente al momento:** Entrega 4 (afecta dominio heredado de Entrega 1)
- **Contexto:** `PersonaJuridica.representante` es hoy un único `PersonaHumana`, usado de igual forma tanto para donantes jurídicos como para entidades beneficiarias. La nota de diseño original del equipo (en `diagramas/ddc/DONACIONES.drawio.xml`, nota *11) aclaraba que debía ser 1 representante para donantes pero una lista de representantes para entidades beneficiarias — el código nunca implementó esa distinción.
- **Fuentes revisadas:** decisiones.md (sin match) / entrega4-requerimientos.md (no cubre este detalle) / PDF consigna pág. 11-12 ("Cada organización tendrá personas representantes habilitadas a operar en su nombre" — no distingue cardinalidad por rol de forma explícita, es genérico en plural para ambos casos).
- **Opciones consideradas:**
  - A) Cardinalidad distinta según el rol (fiel a la nota de diseño original): lista siempre, restringida a 1 elemento cuando la persona jurídica actúa como donante.
  - B) Lista de representantes siempre, sin restricción por rol.
- **Decisión tomada:** Opción A — cardinalidad distinta según el rol, fiel a la nota de diseño original del equipo.
- **Definida por:** usuario

## [D-005] Corrección del bug de concurrencia (colisión de ids) detectado en el escaneo de calidad
- **Fecha:** 2026-09-10
- **Servicio(s) afectado(s):** Donaciones
- **Entrega vigente al momento:** Entrega 4
- **Contexto:** Escaneo de calidad (revisión de código + prueba de estrés en vivo) sobre servicio-donaciones confirmó pérdida silenciosa de datos bajo carga concurrente: 50 altas de donante en paralelo generaron solo 45 registros únicos (3 ids colisionados, 5 registros perdidos), sin ningún error visible. Causa: `Long siguienteId++` no atómico + `ArrayList` no thread-safe en `GestorDonaciones`/`GestorDonantes`, expuestos a multi-threading real de Spring MVC.
- **Fuentes revisadas:** decisiones.md (sin match) / entrega4-requerimientos.md (no cubre este nivel de detalle de implementación) — no es una ambigüedad de dominio/consigna sino una decisión de priorización de esfuerzo, se preguntó directamente al usuario.
- **Opciones consideradas:**
  - A) Esperar a la etapa de persistencia (JPA/MySQL generan ids de forma atómica y transaccional de fábrica).
  - B) Mitigación mínima ahora (`CopyOnWriteArrayList`/`synchronizedList` + `AtomicLong` en los Gestores), a sabiendas de que es un parche transitorio que se descarta al migrar a persistencia.
- **Decisión tomada:** Opción A — esperar a la etapa de persistencia. No se parchea ahora.
- **Definida por:** usuario

## [D-006] Reconciliación `refactor`/`cola-broker` vía port selectivo (RF-3/RF-4)
- **Fecha:** 2026-10-08
- **Servicio(s) afectado(s):** Donaciones, Notificaciones (Logística e Incentivos no se tocaron en esta pasada)
- **Entrega vigente al momento:** Entrega 4
- **Contexto:** `cola-broker` (rama de Agustina Fuentes) implementó RF-3 (cola de notificaciones) y RF-4 (broker multi-proveedor de Logística) sobre una versión pre-refactor del dominio de Donaciones — `DonacionService` en esa rama usa `LogisticaClient`, `DonacionPendienteDTO`/`TimeStampDTO` con forma distinta a la actual, y no vio nunca las skills ni `decisiones.md` (no existían en esa rama). Un merge/rebase de Git no resuelve esto: no es conflicto de texto, es diseño construido sobre dos formas distintas del mismo dominio. Ver `skills/donatrack-branch-reconciliation`, creada a partir de este caso.
- **Fuentes revisadas:** decisiones.md (sin entrada previa sobre esto) / `progress/cola-broker.md` y `progress/refactor.md` (comparación detallada hecha en sesión) / consigna Entrega 4 (RF-3/RF-4 explícitos, sin indicar cómo reconciliar código ya escrito en paralelo — fuera del alcance del PDF).
- **Opciones consideradas:**
  - A) `git merge`/`git rebase` de `cola-broker` sobre `refactor` (o viceversa) y resolver conflictos a mano.
  - B) Port selectivo: clasificar cada pieza nueva en import limpio / reimplementar contra el dominio actual / descartar, sin intentar reconciliar la historia de Git.
- **Decisión tomada:** Opción B. Ejecutado en esta sesión:
  - **Import limpio:** `servicio-notificaciones` completo (no existía en `refactor`), `docker-compose.integration.yml` (infra RabbitMQ), dependencia `spring-boot-starter-amqp` en el pom de Donaciones.
  - **Reimplementado contra el dominio actual:** `LogisticaClient`/`EventoLogisticoDTO`/`EntregaEventoDTO`/`LogisticaBroker` (portados casi sin cambios, movidos de paquete `integration` a `integracion` por consistencia de convención); `RabbitPublicadorEventos` nueva (reemplaza a `NoOpPublicadorEventos` como implementación real de `PublicadorEventosPort`, el seam que ya estaba preparado); `EnvioLogisticaScheduler` nueva (a diferencia de `cola-broker`, que empujaba una donación a la vez al confirmar asignación, reutiliza `DonacionService.obtenerPendientes(page,size)` ya existente para loteo real de ≤100 en horario de baja carga); `EventosLogisticaScheduler` portado con un solo cambio de import (paquete).
  - **Descartado explícitamente:** el árbol `domain/`+`service/`+controllers nuevo de Incentivos en `cola-broker` (servicios stub que devuelven DTOs vacíos, `RankingScheduler` con criterio equivocado — por total de donaciones en vez de por misiones cumplidas). No se mergea nada de ahí.
  - **Diferido, no descartado:** el cableado de `NotificacionesClient` de Incentivos (Consultor → Notificaciones) — `Consultor` es un singleton manual (`getInstance()`), no un bean de Spring, enchufarle un `RabbitTemplate` requiere una decisión de diseño propia (¿se hace `Consultor` un `@Component`? ¿se inyecta desde afuera?) que no se tomó en esta pasada.
- **Definida por:** usuario (aprobó la estrategia de port selectivo antes de ejecutar)

## [D-007] Hueco de dominio: `Donacion` no tiene referencia al `Donante`
- **Fecha:** 2026-10-08
- **Servicio(s) afectado(s):** Donaciones
- **Entrega vigente al momento:** Entrega 4
- **Contexto:** Al implementar `RabbitPublicadorEventos` (D-006) se encontró que `PublicadorEventosPort.publicar(tipoEvento, payload)` no puede resolver destinatario+contacto para los eventos dirigidos a la persona donante (ej. "tu donación fue asignada", Entrega 2) porque `Donacion` solo referencia `EntidadBeneficiaria`, nunca al `Donante` que la originó. `NotificacionRequestDTO.contacto` es `@NotBlank` en Notificaciones — no hay forma de enviar ese caso sin inventar un contacto.
- **Fuentes revisadas:** decisiones.md (sin match) / DDC de Donaciones (`SolicitudDonacion`/`Donacion`/`Donante` — tampoco modela esa referencia, confirma que es un hueco real de diseño, no un olvido de implementación) / consigna (no detalla el modelo de datos a este nivel).
- **Opciones consideradas:**
  - A) Acotar el alcance: `RabbitPublicadorEventos` solo notifica cuando el payload trae una `EntidadBeneficiaria` resolvible; cuando no, loguea advertencia y no publica. No se inventa un contacto.
  - B) Agregar la referencia `Donacion → Donante` (o la cadena que corresponda vía `SolicitudDonacion`) al dominio antes de seguir.
  - C) Publicar con un contacto placeholder/log-only.
- **Decisión tomada:** Opción A. Pendiente para una sesión futura: decidir cómo modelar la referencia de vuelta al donante (opción B) — hoy ningún evento de Entrega 2 dirigido al donante se puede notificar realmente (ver `progress/refactor.md`).
- **Definida por:** usuario
