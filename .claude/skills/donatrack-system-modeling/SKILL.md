---
name: donatrack-system-modeling
description: Mantenimiento de los diagramas de clases (DDC) y de entidad-relación físico (DER) de DonaTrack en draw.io, sincronizados con el código real de cada servicio. Usar al crear, modificar o eliminar clases de dominio, entidades JPA, relaciones entre ellas, o migraciones de esquema — el diagrama se actualiza como parte del mismo cambio, no como tarea aparte. También usar al generar el entregable "Modelo del Dominio" o "Modelo de datos" que pide cada entrega.
---

# Modelado del Sistema — DDC y DER en draw.io (DonaTrack)

## Convención de archivos (carpeta `diagramas/` existente)
```
diagramas/
├── ddc/
│   ├── DONACIONES.drawio.xml
│   ├── LOGISTICA.drawio.xml
│   ├── INCENTIVOS.drawio.xml
│   └── NOTIFICACIONES.drawio.xml
└── der/
    ├── DONACIONES.drawio.xml
    ├── LOGISTICA.drawio.xml
    ├── INCENTIVOS.drawio.xml
    └── NOTIFICACIONES.drawio.xml
```
- Naming real del repo: **nombre del servicio en MAYÚSCULA + extensión `.drawio.xml`** (no `.drawio` a secas) — confirmado contra los archivos ya existentes en `diagramas/ddc/`. Respetar esta convención al crear los archivos que falten en `diagramas/der/` (hoy vacía), no introducir una convención nueva en minúscula.
- Un archivo por servicio y por tipo, nunca un diagrama mezclando DDC y DER, y nunca un solo archivo con los cuatro servicios juntos — igual criterio que se usó para las colecciones de Bruno (paralelismo entre servicios, revisiones en PR acotadas al servicio que cambió).

## Cómo edita el agente un `.drawio`
- Es XML (`mxGraph`) plano — diffable y versionable en git sin Git LFS. Se puede leer y editar como texto.
- **No regenerar el diagrama entero** cuando el cambio es puntual (una clase nueva, un atributo, una relación): localizar el/los nodos (`mxCell`) afectados y modificar solo eso, preservando posición y estilo de todo lo que no cambió. Regenerar desde cero solo si el usuario lo pide explícitamente o si el diagrama existente quedó tan desactualizado que reconstruirlo es más confiable que parchearlo (avisar antes de hacerlo).
- Si el cambio es grande (nuevo servicio, refactor de agregados), está bien proponer layout nuevo — pero como parte de la propuesta técnica del ciclo estándar (`donatrack-audit-and-fix-workflow`), no como efecto secundario silencioso de otro cambio.

## Regla de consistencia dura
El diagrama nunca puede divergir de la fuente real:
- **DDC** ↔ clases de dominio efectivamente implementadas (no las que "deberían estar" según la consigna si el código todavía no llegó ahí — el diagrama documenta el estado real, la brecha contra la consigna se registra aparte, en `donatrack-requirements-guardrails`).
- **DER físico** ↔ migraciones Flyway reales aplicadas (ver `donatrack-jpa-persistence`), no el modelo de dominio disfrazado de diagrama de datos.
- Si un cambio de código toca clases de dominio o entidades/migraciones, el plan de implementación de `donatrack-audit-and-fix-workflow` debe incluir explícitamente el paso "actualizar `diagramas/ddc/<servicio>.drawio`" y/o "`diagramas/der/<servicio>.drawio`" — mismo patrón ya aplicado con la re-sincronización de Bruno para endpoints.

## Qué debe representar el DDC por servicio
- Clases de dominio con atributos tipados y los métodos relevantes al comportamiento de negocio (ej. `Donacion.segmentar()`, transición de estados), no getters/setters.
- Herencia con el estereotipo UML correcto: `PersonaDonante` → `PersonaHumana` / `PersonaJuridica`; `NecesidadMaterial` → `NecesidadRecurrente` / `NecesidadExtraordinaria`.
- Multiplicidades explícitas en cada asociación (`1`, `0..*`, `1..*`, etc.), especialmente en las relaciones que la consigna resalta: `DonacionOriginal` 1 → N `Donacion` (segmentadas), `EntidadBeneficiaria` 1 → N `NecesidadMaterial`.
- Patrones de diseño aplicados visibles cuando corresponda (ej. marcar con nota/estereotipo el Strategy de algoritmos de asignación, el Strategy de medios de notificación, el Adapter/Strategy del broker de logística) — ayuda a que el diagrama sirva también como evidencia de las "justificaciones de diseño" que pide cada entrega.

## Qué debe representar el DER físico por servicio
- Tablas reales (no clases), con tipos de columna concretos (`VARCHAR(255)`, `DECIMAL(10,2)`, etc.), claves primarias, claves foráneas con su cardinalidad, y constraints de unicidad (ej. email de persona donante, patente de camión).
- Nombre de esquema visible o indicado en el título del diagrama (`donaciones.donacion`, no solo `donacion`), para que quede claro que es un esquema dentro de la base compartida, no una base aparte.
- Tablas de auditoría/historial (ej. `donacion_estado_historico`) representadas como tablas propias con su FK a la tabla principal, no como un campo suelto.

## Checklist antes de dar por actualizado un diagrama
1. Cada clase/tabla del diagrama tiene una clase/entidad/migración real correspondiente en el código — y viceversa, no quedaron huérfanos de un refactor anterior.
2. Los tipos de atributo/columna coinciden con el código real, no con lo que "tendría sentido".
3. Las multiplicidades y las FKs reflejan las relaciones tal como están implementadas, no como se planeaban originalmente.
4. La herencia está modelada con el estereotipo correcto en el DDC, y resuelta según la estrategia elegida (`JOINED`/`SINGLE_TABLE`, ver `donatrack-jpa-persistence`) en el DER.
5. El archivo quedó en la carpeta y con el nombre que corresponde según la convención de arriba.

## Qué NO hacer
- No generar el DER como una copia del DDC con nombres en snake_case — son artefactos distintos (uno es diseño OO, el otro es esquema físico) y la consigna los pide como entregables separados.
- No dejar un diagrama "a medio actualizar" cuando el plan de implementación ya se dio por terminado — es parte del mismo entregable, no una tarea de limpieza posterior.
- No versionar exports (`.png`/`.svg`) como fuente de verdad — el `.drawio` es la fuente; si se necesita una imagen para un documento, exportarla como paso final, no editarla directamente.
