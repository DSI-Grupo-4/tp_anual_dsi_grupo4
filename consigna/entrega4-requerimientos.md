# DonaTrack — Entrega 4: Persistencia, Integración y Despliegue
Curso K3002 · TP Anual 2026 · Documento de trabajo para IA asistiendo al equipo

> Este documento no reemplaza al PDF de cátedra, es su destilado operativo para que los agentes de IA (Claude Code u otros) tengan un checklist accionable y no trabajen "a ciegas" contra el enunciado completo en cada tarea.

## Objetivos declarados de la entrega
- Incorporar persistencia de datos en un medio relacional.
- Incorporar la técnica de mapeo objeto-relacional (ORM).
- Incorporar estrategias y patrones de integración (mensajería asíncrona, broker).

## Contexto heredado que condiciona esta entrega
La Entrega 4 **no es un punto de partida**, es la maduración de decisiones ya tomadas en Entregas 1-3. Antes de tocar código de persistencia/integración, hay que respetar:
- La segmentación de donaciones por subcategoría (Entrega 1) y su responsabilidad (donación o fachada).
- La máquina de estados de la donación y su trazabilidad/auditoría (Entrega 2, Figura 2 del PDF).
- El Strategy de algoritmos de asignación (Compatibilidad Semántica / Prioridad a sub-atendidos).
- El manejo de pérdida de progreso en misiones tipo "Racha" (Servicio de Incentivos).
- Que el Servicio de Logística **no** invoca a Donaciones/Incentivos ni se comunica con Notificaciones (restricción explícita de Entrega 3, sigue vigente).
- Un único motor de base de datos relacional (MySQL8 según diagrama de despliegue orientativo, Figura 1), con **un esquema por servicio** — no una base por servicio.
- El Servicio de Incentivos ya corre un workflow **n8n** (contenedor Docker) para la difusión de insignias en redes sociales y el ranking mensual (Entrega 2). Este componente debe seguir apareciendo en el `docker-compose.yml` y en el diagrama de despliegue/componentes actualizado de esta entrega — no es infraestructura "aparte", es parte de la arquitectura del servicio.

## Checklist de madurez heredado de Entrega 3 (PDF pág. 20-22)
La cátedra fue explícita: *"Es importante que para esta entrega los requerimientos y decisiones de entregas 1 y 2 estén maduros"* — esto no es historia vieja, es criterio de evaluación vigente para Entrega 4. Verificado contra código el 2026-10-08 (ver `progress/plan-ataque-entrega4.md`, sección "Hallazgos de auditoría").

Servicio de Donaciones (Modelo de Objetos y Código):
1. Donaciones originales vs. segmentadas, segmentación responsabilidad de la donación/fachada (Entrega 1).
2. Necesidades recurrentes bien definidas (Entrega 1).
3. Importación masiva de donantes en CSV funcional (Entrega 1).
4. Proceso de asignación con Strategy o similar (Entrega 2).

Endpoints Donaciones: 5. Exposición correcta de operaciones (Entrega 2).

Servicio de Incentivos (Modelo de Objetos, Código):
6. Manejo de pérdida de misiones (ej. "racha" se pierde sin donar en un mes) (Entrega 2).

Workflow de publicación (Código y N8N): 7. Integración con redes sociales vía N8N (Entrega 2).

Endpoints Incentivos: 8. Exposición correcta de operaciones (Entrega 2). 9. **Las donaciones deben impactar en el cálculo de progreso** (Entrega 2).

Servicio de Notificaciones (Modelo de Objetos y Código):
10. Envío por diversos medios vía Strategy o similar (Entrega 2).
11. Notificación a donante sin interacción en más de 20 días (Entrega 2).
12. Notificación a entidad beneficiaria cuando se le asigna una donación (Entrega 2).
13. Notificación a donante cuando cumple una misión (Entrega 2).
14. Notificación a donante cuando cambia de categoría (Entrega 2).

Endpoints Notificaciones: 15. Exposición correcta de operaciones (Entrega 2).

## Requerimientos funcionales (RF)

| ID | Requerimiento | Fuente |
|---|---|---|
| RF-1 | Cada servicio debe persistir su información en un motor relacional, respetando el criterio de un esquema por servicio dentro de la misma base. | Contexto general + Entrega 4 |
| RF-2 | Cada servicio debe mapear su modelo de objetos al modelo relacional (ORM), manteniendo separado el modelo de dominio de las entidades de persistencia donde corresponda. | Entrega 4 / Decálogo #3 |
| RF-3 | La integración entre los servicios de dominio (Donaciones, Incentivos, Logística) y el Servicio de Notificaciones debe ser **asíncrona**, a través de una cola de mensajes. | Entrega 4 — Cola de mensajes |
| RF-4 | Debe implementarse un **broker** para la integración entre el Servicio de Donaciones y el Servicio de Logística, capaz de seleccionar entre más de un proveedor de logística (el propio servicio construido + al menos otro potencial equivalente). | Entrega 4 — Broker de Integración |
| RF-5 | El Servicio de Logística debe desplegarse accesible vía web a través de sus URIs. | Entrega 4 — Despliegue |
| RF-6 | Actualizar el modelo de clases de cada servicio para reflejar persistencia/integración. | Entregables |
| RF-7 | Generar diagrama entidad-relación **físico** por cada servicio. | Entregables |
| RF-8 | Actualizar el diagrama de componentes incluyendo los componentes de integración de esta entrega (cola, broker). | Entregables |

## Requerimientos no funcionales (RNF)

| ID | Requerimiento | Justificación / Fuente |
|---|---|---|
| RNF-1 | **Disponibilidad/Resiliencia**: la asincronía hacia Notificaciones debe evitar que picos de carga o fallas transitorias de ese servicio degraden la disponibilidad de los servicios de dominio. | Enunciado explícito de Entrega 4 |
| RNF-2 | **Extensibilidad / Open-Closed**: el broker de Logística debe permitir agregar/seleccionar proveedores sin modificar la lógica interna del Servicio de Donaciones. | Enunciado explícito ("más de 1 servicio de logística disponible") |
| RNF-3 | **Desacoplamiento arquitectónico**: Logística sigue sin invocar a Donaciones/Incentivos ni comunicarse con Notificaciones (restricción heredada de Entrega 3). | Entrega 3, reafirmada |
| RNF-4 | **Separación dominio/arquitectura/tecnología**: el ORM y el motor de mensajería son medios, no deben filtrar detalles de infraestructura al modelo de dominio. | Decálogo #3 |
| RNF-5 | **Trazabilidad de diseño**: toda decisión debe quedar justificada por escrito (documento + diagramas), incluyendo alternativas descartadas. | Decálogo #8 + Entregables |
| RNF-6 | **Control de costos operativos**: el despliegue de Logística puede pausarse para reducir consumo, pero debe poder reactivarse para la defensa. | Entrega 4 — Requerimiento de Despliegue |
| RNF-7 | **Consistencia con historial**: el mapeo ORM no debe romper invariantes ya implementados (estados, auditoría, progreso de misiones) de entregas previas. | Revisión general Entrega 3 |
| RNF-8 | **Documentabilidad de API**: cada servicio debe seguir exponiendo sus endpoints documentados (Postman/Swagger), ya exigido desde Entrega 3 y vigente. | Entrega 3 — Documentación general |

## Entregables formales de Entrega 4
1. Modelo de Clases actualizado por servicio.
2. Modelo de datos: diagrama entidad-relación físico por servicio.
3. Justificaciones de Diseño (documento + diagramas complementarios).
4. Diagrama de Componentes actualizado (con componentes de integración: cola, broker).
5. Documento explicativo de arquitectura (patrones, capas) — sin detalle de componentes, con foco en justificar decisiones. *(Nota de cátedra: puntos 5 y 6 se trabajan durante agosto en el curso — verificar si ya está cubierto en versiones previas del TP).*
6. Implementación de los requerimientos de integración de esta entrega.
7. Despliegue del Servicio de Logística.

## Fuera de alcance explícito de esta entrega
- No se pide todavía Arquitectura Web MVC ni el front SSR (Entrega 5).
- No se pide despliegue, observabilidad ni seguridad end-to-end del resto de servicios (Entrega 6) — solo Logística.
- No se debe invertir tiempo en UI/UX nueva; eso ya se resolvió en Entrega 1 (bocetos) y se madura en Entrega 4/5 de maquetado.
