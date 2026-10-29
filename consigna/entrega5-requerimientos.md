# DonaTrack — Entrega 5: Persistencia
Curso K3002 · TP Anual 2026 · Documento de trabajo para IA asistiendo al equipo

> Este documento no reemplaza al PDF de cátedra, es su destilado operativo para que los agentes de IA (Claude Code u otros) tengan un checklist accionable y no trabajen "a ciegas" contra el enunciado completo en cada tarea.

## Objetivos declarados de la entrega
- Incorporar nociones de persistencia de datos en un medio relacional.
- Incorporar nociones de la técnica de mapeo objeto-relacional (ORM).
- Incorporar nociones de desnormalizaciones del modelo relacional.

## Contexto heredado que condiciona esta entrega — ⚠️ pivot, no continuidad lineal

A diferencia de Entrega 4 (que sí fue una maduración directa de lo ya construido), **Entrega 5 revierte parcialmente una decisión ya implementada**: hasta ahora, Logística persiste en MySQL relacional igual que el resto de los servicios (D-025, D-026, D-027). La consigna nueva pide explícitamente lo contrario para Logística. Esto no es responsabilidad de una mala decisión previa — el enunciado vigente al momento de D-025/D-027 no distinguía motores por servicio; la distinción políglota es nueva en esta versión del PDF.

**Estado real por servicio al momento de leer esta entrega:**
- **Donaciones**: persistencia relacional (MySQL + JPA) ya implementada y validada (D-030). Cumple el requisito de esta entrega para servicios "no Logística" sin trabajo adicional de motor, salvo lo que pida desnormalización puntual.
- **Incentivos**: ídem, ya implementado (D-028). Mismo caso que Donaciones.
- **Logística**: persistencia relacional (MySQL + JPA) ya implementada (D-025, D-027) — **pero la consigna ahora exige documental**. Esto implica migrar el mapeo de entidades de JPA a un ODM y el esquema de `logistica.sql` deja de ser la fuente de verdad. Ya existe una rama local `mongodb` (no mergeada, con commits de un compañero: "Configurar MongoDB Atlas para Render", "Primera version Base de Datos del Servicio de Logistica") que parece ser trabajo adelantado exactamente sobre este requisito — conviene revisarla antes de empezar desde cero, en vez de duplicar el trabajo (ver `donatrack-branch-reconciliation`).
- **Notificaciones**: **sin persistencia todavía** (sigue en memoria + historial en archivos locales, ver `persistencia-jpa-mysql-decidida-diferida` en memoria del agente). Esta entrega la vuelve obligatoria: "Se deberán persistir las entidades del modelo planteado para cada servicio" no hace excepción con Notificaciones, y al no ser Logística, le corresponde el motor relacional.

Mismo límite heredado de D-012/D-030: no mapear entidades sin un DER aprobado antes — para Notificaciones y para el nuevo modelo documental de Logística, el DER/modelo de datos tiene que salir primero (aquí, además, es un entregable explícito: "Actualizaciones de Modelo del Dominio, Modelo de Datos y Justificaciones de Diseño").

## Requerimientos funcionales (RF)

| ID | Requerimiento | Fuente |
|---|---|---|
| RF-1 | Se deben persistir las entidades del modelo de cada servicio (incluye Notificaciones, hoy sin persistencia). | Entrega 5 — Requerimientos detallados #1 |
| RF-2 | El Servicio de Logística debe usar una **base de datos documental**, con un **ODM** para el mapeo objeto-documento. | Entrega 5 — Requerimientos detallados #2 |
| RF-3 | Los servicios restantes (Donaciones, Incentivos, Notificaciones) deben persistir en **base de datos relacional**, con **ORM**. | Entrega 5 — Requerimientos detallados #2 |
| RF-4 | Cada servicio relacional debe tener su propio esquema; se permite compartir un mismo motor entre ellos por simplicidad (ya implementado: un único MySQL, un esquema por servicio). | Entrega 5 — Alcance |
| RF-5 | Se deben contemplar estrategias de **desnormalización** cuando sea necesario para optimizar consultas de lectura. | Entrega 5 — Requerimientos detallados #3 |
| RF-6 | Actualizar el Modelo del Dominio, el Modelo de Datos y las Justificaciones de Diseño de cada servicio afectado. | Entregables |
| RF-7 | Actualizar el Diagrama de Componentes para reflejar la estrategia de persistencia políglota. | Entregables |

## Requerimientos no funcionales (RNF)

| ID | Requerimiento | Justificación / Fuente |
|---|---|---|
| RNF-1 | **Persistencia políglota coherente**: convivencia de un motor relacional (Donaciones/Incentivos/Notificaciones) y uno documental (Logística) sin que ninguno de los dos filtre detalles de infraestructura al modelo de dominio. | Enunciado explícito de Entrega 5 + Decálogo #3 |
| RNF-2 | **Separación dominio/persistencia**: igual que en Entrega 4 (D-028/D-030), las entidades de persistencia no deben condicionar las clases de dominio ya validadas en entregas previas. | Decálogo #3, consistente con D-030 |
| RNF-3 | **No regresión funcional**: la migración de Logística de relacional a documental no debe romper comportamiento ya probado (ciclo de vida de rutas/entregas, eventos logísticos, broker con Donaciones de Entrega 4). | Decálogo #2 (sistema evolutivo) + RNF-7 heredado de Entrega 4 |
| RNF-4 | **Trazabilidad de diseño**: la decisión de qué desnormalizar, y la migración de Logística a documental, deben quedar documentadas por escrito con alternativas consideradas. | Decálogo #8 |
| RNF-5 | **No duplicar trabajo ya iniciado**: antes de migrar Logística a Mongo desde cero, evaluar y reconciliar el trabajo ya existente en la rama `mongodb` (ver `donatrack-branch-reconciliation`). | Hallazgo de esta auditoría, no está en el PDF |

## Entregables formales de Entrega 5
1. Actualización de Modelo del Dominio, Modelo de Datos y Justificaciones de Diseño (por servicio afectado).
2. Actualización del Diagrama de Componentes.
3. Implementación de los requerimientos de esta entrega en código y bases de datos (ambos motores).

## Fuera de alcance explícito de esta entrega
- No se pide todavía interfaz de usuario, maquetado ni arquitectura MVC (Entrega 6).
- No se pide observabilidad, monitoreo, rate limiting, gRPC ni GraphQL (Entrega 7).
- La integración asíncrona (cola de notificaciones) y el broker de Logística ya se piden desde Entrega 4 — esta entrega no agrega nuevos requisitos de integración, solo de persistencia.
