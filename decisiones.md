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

## [D-008] Resuelve D-007: `donanteId` en la carga de donación
- **Fecha:** 2026-10-08
- **Servicio(s) afectado(s):** Donaciones
- **Entrega vigente al momento:** Entrega 4 (fase de modelado, previa a persistencia — decisión explícita del usuario de resolver consistencia de modelado y comunicación antes de la Entrega 4 propiamente)
- **Contexto:** Al investigar D-007 en profundidad se encontró que el hueco era más grande de lo documentado: `CargaDonacionDTO`/`SolicitudDonacion`/`Donacion` no capturaban en ningún lado quién hacía la donación — ni siquiera había un campo `donanteId` en el alta. Esto no es solo un problema de notificaciones: la consigna (Entrega 1, Persona administradora, ítem 15) exige explícitamente que la persona administradora registre donaciones "asociándolas a la persona donante correspondiente", requerimiento incumplido hasta hoy.
- **Fuentes revisadas:** decisiones.md (D-007, mismo tema, sin resolver el modelado) / PDF consigna, Entrega 1 "Persona administradora" ítem 15 (explícito) / código (`CargaDonacionDTO`, `SolicitudDonacion`, `DonacionController.crear` — confirmado el hueco).
- **Opciones consideradas:** no hubo alternativas reales — es un requerimiento explícito incumplido, no una ambigüedad de diseño.
- **Decisión tomada:** `CargaDonacionDTO` gana `donanteId` (obligatorio); `DonacionService.crear()` resuelve el `Donante` vía `GestorDonantes.buscarPorId` y lo propaga a `SolicitudDonacion` → cada `Donacion` segmentada. `DonacionDTO` expone `donanteId` en la respuesta. Esto habilita (parcialmente, ver D-009) que `RabbitPublicadorEventos` notifique también al donante, no solo a la entidad beneficiaria.
- **Definida por:** usuario (aprobó la Fase 1 de modelado donde este punto se identificó como corrección directa, no como decisión a consultar)

## [D-009] Contrato de mensaje hacia Notificaciones ensanchado (`tipoEvento` + `eventoId`)
- **Fecha:** 2026-10-08
- **Servicio(s) afectado(s):** Notificaciones, Donaciones, Incentivos
- **Entrega vigente al momento:** Entrega 4
- **Contexto:** El DDC de Notificaciones modela un diseño rico (`GestorNotificaciones` con 8 métodos específicos por evento + `ENUM EstadoEntrega`) que nunca se implementó — el código real usaba un contrato genérico (`mensaje`/`medio`/`contacto`/`servicioOrigen`) sin forma de deduplicar reintentos de la cola (gap ya señalado contra `skills/donatrack-async-notifications`, que exige idempotencia por `eventoId`). El usuario, al definir la Fase 1 de modelado/comunicación previa a persistencia, pidió explícitamente resolver esto antes de seguir.
- **Fuentes revisadas:** decisiones.md (D-006, documenta que el gap de idempotencia ya estaba señalado) / DDC de Notificaciones (el diseño de 8 métodos + `EstadoEntrega`) / `skills/donatrack-async-notifications` (exige idempotencia por `eventoId` explícitamente).
- **Opciones consideradas:**
  - A) Ensanchar el contrato ahora: agregar `tipoEvento`+`eventoId` a `NotificacionRequestDTO`, implementar idempotencia real en `NotificacionService`, actualizar los 3 productores.
  - B) Mantener el contrato genérico y dejarlo para después de persistencia.
- **Decisión tomada:** Opción A, con un matiz de alcance que el agente propuso y el usuario no objetó: el `mensaje` lo sigue armando quien publica (Donaciones/Incentivos), **no** se mueve el templating de texto hacia Notificaciones — implementar los 8 métodos específicos de `GestorNotificaciones` con conocimiento del dominio de cada servicio productor es un salto de arquitectura mayor al pedido ("ensanchar el contrato"), queda como backlog aparte si se decide abordarlo. Se implementó: `NotificacionRequestDTO`/`Notificacion`/`NotificacionResponseDTO` ganan `tipoEvento`+`eventoId`; `NotificacionService.enviarNotificacion` deduplica por `eventoId` (mapa separado, un reintento de la cola con el mismo id devuelve la notificación ya existente sin volver a despachar); `RabbitPublicadorEventos` (Donaciones) genera `eventoId` por mensaje y usa el `tipoEvento` real del evento (no siempre el genérico `CAMBIO_ESTADO_DONACION`) vía un nuevo campo `origenEvento` en `CambioEstadoDTO`; `EventosLogisticaScheduler` setea ese campo con el tipo real del evento de Logística, así los 3 casos de Entrega 3 (ruta planificada/iniciada, entrega confirmada/fallida) generan mensajes con texto distinto. Validado en vivo: mismo `eventoId` publicado dos veces → una sola notificación registrada.
- **Definida por:** usuario

## [D-010] Hueco de dominio en Incentivos: `Donante` sin ninguna fuente de contacto
- **Fecha:** 2026-10-08
- **Servicio(s) afectado(s):** Incentivos
- **Entrega vigente al momento:** Entrega 4
- **Contexto:** Al cablear `Consultor` hacia RabbitMQ (reemplazo del `RestTemplate` síncrono que violaba la restricción de integración asíncrona de Entrega 4) se encontró que el `Donante` de Incentivos (clase propia de este servicio, distinta de la de Donaciones) no tiene ningún campo de contacto, y el endpoint que podría traerlo desde Donaciones (`POST /donantes/{id}/actividad-donacion`) ni siquiera está invocado por nadie todavía. Mismo tipo de límite que D-007/D-008, pero en otro servicio.
- **Fuentes revisadas:** decisiones.md (D-007/D-008, mismo patrón de hueco) / código (`Donante.java` de Incentivos, `DatosDonacionDTO`, `IncentivosController` — confirmado que no hay ningún caller real del endpoint de actividad desde Donaciones).
- **Opciones consideradas:**
  - A) Dejar la plumbing de RabbitMQ lista (cliente real, config) y agregar campos opcionales de contacto a `Donante` + `DatosDonacionDTO` para cuando alguien los popule, documentando el límite explícitamente — no inventar un contacto.
  - B) No tocar nada hasta resolver el cableado completo Donaciones→Incentivos de actividad de donación.
- **Decisión tomada:** Opción A. `Donante.actualizarContactoSiFalta(medio, contacto)` (mismo patrón que `actualizarNombreSiFalta`), `DatosDonacionDTO` gana `donanteMedioContacto`/`donanteContacto` opcionales, `NotificacionesClient` nuevo (RabbitMQ real) reemplaza al `RestTemplate`, pero sigue sin poder notificar a nadie hoy porque nada popula el contacto todavía — logueado como advertencia, no como error, mismo comportamiento que D-007 en Donaciones.
- **Definida por:** usuario (mismo criterio que D-008/D-009, dentro de la Fase 1 de modelado)

## [D-011] Prohibición de referenciar requerimientos/notaciones internas desde comentarios del código de proyecto
- **Fecha:** 2026-10-08
- **Servicio(s) afectado(s):** Todos (Donaciones, Logística, Incentivos, Notificaciones)
- **Entrega vigente al momento:** Entrega 4
- **Contexto:** El código de este repo es un trabajo grupal. El usuario señaló que el repo de contexto de trabajo con agentes (`decisiones.md`, `plan-ataque-entrega4.md`, `progress/refactor.md`, `skills/`) es suyo y de los agentes que despliega para la tarea, no del resto del equipo. Hasta hoy varios comentarios en el código de producción referenciaban directamente notaciones internas (`D-00x`, `RF-x`, "Entrega N", "ver decisiones.md", "ver skills/...", "consigna pág. N", nombres de test con "DelPdf") que no tienen sentido fuera de ese contexto y mezclan el proceso de trabajo con IA con el código compartido del equipo.
- **Fuentes revisadas:** código del proyecto (`grep` de `D-00x`/`RF-x`/`Entrega N`/`decisiones.md`/`skills/`/`consigna` en todos los `*.java`, ~20 ocurrencias encontradas).
- **Opciones consideradas:** no hubo alternativas de diseño — es una regla de higiene de comentarios, no una decisión de arquitectura.
- **Decisión tomada:** los comentarios del código de producción y de tests explican el *por qué* en términos funcionales/de dominio (ej. "quién hizo la donación — antes no se registraba en ningún lado"), sin citar el identificador de la decisión, el número de requerimiento ni el número de entrega. Esas referencias viven solo en `decisiones.md`/`plan-ataque-entrega4.md`/`progress/refactor.md` (repo de contexto del usuario), nunca en el código fuente del proyecto. Regla aplicada retroactivamente a las ~20 ocurrencias existentes y vigente para todo trabajo futuro.
- **Definida por:** usuario

## [D-012] Arquitectura de persistencia decidida y registrada de antemano, implementación diferida hasta tener el DER
- **Fecha:** 2026-10-08
- **Servicio(s) afectado(s):** Todos (Donaciones, Logística, Incentivos, Notificaciones)
- **Entrega vigente al momento:** Entrega 4 (Tier 1 del plan de ataque — ver `plan-ataque-entrega4.md`)
- **Contexto:** El usuario definió que la persistencia es intencionalmente lo último que se toca (después de la Fase 1 de modelado, la Fase 2 de endpoints y la Fase 3 de documentación/pruebas de API), porque hoy cada servicio maneja su estado de un modo distinto (todo en memoria vía singletons/listas) y prefiere estabilizar antes modelado y comunicación. Aun así quiso dejar registrada de antemano la decisión tecnológica, para no tener que redecidirla cuando llegue el momento, y fue explícito en que no es "hacer consultas a pulmón" sino agregar una capa de implementación real con un ORM.
- **Fuentes revisadas:** `decisiones.md` (D-001, que ya fija MySQL + un esquema por servicio para la organización de `docker-compose`, pero no la tecnología de acceso a datos) / `pom.xml` de `servicio-donaciones` (ya tiene `spring-boot-starter-data-jpa` + H2 sin usar, scaffolding muerto) / los 4 servicios ya corren sobre Spring Boot.
- **Opciones consideradas:**
  - A) Spring Data JPA en los 4 servicios, contra MySQL real en contenedor Docker (+ contenedor de MySQL Workbench para administración visual).
  - B) Persistencia distinta por servicio según conveniencia (ej. H2 en memoria para algunos, Mongo para otros).
  - C) Un ORM distinto a JPA (ej. jOOQ, MyBatis) sobre el mismo MySQL.
- **Decisión tomada:** Opción A. Spring Data JPA en los 4 servicios — es la opción más directa dado que todo el stack ya es Spring Boot — contra una única instancia de MySQL real (no H2) levantada en contenedor Docker, con un esquema por servicio (consistente con D-001). Se suma un contenedor de MySQL Workbench para poder administrar/inspeccionar la base visualmente durante el desarrollo. **Condición explícita del usuario, que bloquea esta decisión hasta nuevo aviso:** no se implementa nada de esto (ni entidades `@Entity`, ni el `docker-compose` de MySQL, ni Workbench) hasta tener resuelto el DER necesario por servicio — ver Tier 4 #10 de `plan-ataque-entrega4.md`, hoy `PENDIENTE`, que pasa a ser prerrequisito explícito de este ítem. Mapear entidades JPA sobre un dominio todavía no decidido en sus cardinalidades/claves arriesga repetir el mismo tipo de hueco que ya costó D-007/D-008.
- **Definida por:** usuario
