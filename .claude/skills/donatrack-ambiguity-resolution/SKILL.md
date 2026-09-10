---
name: donatrack-ambiguity-resolution
description: Cómo resolver cualquier ambigüedad del dominio o de la consigna de DonaTrack antes de proponer una solución técnica, y qué hacer cuando el PDF de la consigna cambia (nueva entrega, corrección de la cátedra). Usar SIEMPRE que un requerimiento no esté claro, que falte un dato concreto (umbral, formato, valor por defecto) en la consigna, que dos fuentes parezcan contradecirse, o que se detecte una modificación en el archivo del PDF de consigna dentro del repo — en ese último caso, usar esta skill ANTES de continuar cualquier otra tarea.
---

# Resolución de Ambigüedades — DonaTrack

Regla de fondo: **ante ambigüedad, siempre se pregunta, nunca se asume en silencio.** Pero preguntar es el último paso, no el primero — antes hay que agotar las fuentes ya disponibles para no hacerle al usuario una pregunta cuya respuesta ya está escrita en algún lado.

## Orden de resolución obligatorio (en este orden, sin saltear pasos)

1. **`decisiones.md`** (bitácora de decisiones, ver formato abajo): ¿esta misma ambigüedad ya se resolvió antes? Si sí, aplicar la decisión registrada y avisar que se está reutilizando una decisión previa (no volver a preguntar lo mismo).
2. **El documento de requerimientos destilados de la entrega vigente** (ej. `entrega4-requerimientos.md`): ¿está explícito ahí, aunque con otras palabras?
3. **El PDF de consigna original**, releído en el momento (no de memoria de una lectura anterior en la sesión) — porque el PDF se actualiza a medida que la cátedra avanza entregas, y una lectura vieja puede estar desactualizada. Buscar en la sección específica del servicio/entrega involucrado, no solo en el resumen.
4. Si después de los tres pasos anteriores la ambigüedad sigue sin resolverse: **recién ahí se pregunta al usuario**, presentando:
   - Qué es concretamente lo ambiguo (el hueco puntual, no una descripción vaga).
   - 2-3 interpretaciones razonables con su trade-off técnico, si existen.
   - Qué fuentes ya se revisaron (para que quede claro que no se está preguntando por pereza de buscar).

## Formato de la bitácora `decisiones.md` (versionada en el repo, raíz o `docs/`)

```markdown
## [D-001] <título corto de la ambigüedad>
- **Fecha:** 2026-09-10
- **Servicio(s) afectado(s):** Logística, Donaciones
- **Entrega vigente al momento:** Entrega 4
- **Contexto:** qué generó la duda (qué se estaba implementando cuando apareció)
- **Fuentes revisadas:** decisiones.md (sin match) / entrega4-requerimientos.md (sin match) / PDF consigna sección X (sin definición explícita)
- **Opciones consideradas:** A) ... B) ...
- **Decisión tomada:** ...
- **Definida por:** usuario / cátedra (aclaración explícita del PDF)
```
- Cada entrada tiene un ID correlativo (`D-001`, `D-002`, ...) para poder referenciarla desde código/PR/otros documentos.
- Se agrega una entrada **solo** cuando la ambigüedad llegó hasta el paso 4 y el usuario la resolvió — las que se resuelven en los pasos 1-3 no generan entrada nueva (ya estaban resueltas), pero si el agente las reutiliza vale la pena mencionar el ID en el chat para trazabilidad.
- No editar retroactivamente una decisión ya tomada si cambian las circunstancias — se agrega una entrada nueva que referencia y reemplaza a la anterior (`D-007 (reemplaza a D-002)`), para no perder el historial de qué se decidió y por qué en cada momento.

## Trigger especial: el PDF de consigna cambió
El repo va a detectar cuando el archivo del PDF de consigna se modifica (nuevo commit que lo toca — diff de contenido, no solo de metadata). Cuando esto pasa, **antes de continuar con cualquier otra tarea pendiente**:

1. Releer el PDF completo (no solo la sección que cambió, porque una entrega nueva puede reinterpretar contexto general que afecta a entregas ya cerradas).
2. Releer el/los documentos de requerimientos destilados existentes (`entregaN-requerimientos.md`) y marcar qué quedó desactualizado.
3. Repasar **todas** las skills del paquete (`donatrack-git-workflow`, `donatrack-requirements-guardrails`, `donatrack-jpa-persistence`, `donatrack-async-notifications`, `donatrack-logistics-broker`, `donatrack-docker-deploy`, `donatrack-api-docs-bruno`, `donatrack-system-modeling`, `donatrack-audit-and-fix-workflow`) contra el PDF actualizado, buscando específicamente:
   - Restricciones que cambiaron (ej. si una futura entrega relaja o endurece la regla de "Logística no habla con Notificaciones").
   - Nuevos servicios/componentes no contemplados en ninguna skill.
   - Entregables nuevos que ninguna skill cubre todavía.
4. Presentar al usuario un resumen de qué cambió en el PDF y qué skills/documentos quedaron desalineados — **no corregirlas de forma autónoma**, esto es en sí mismo un caso a resolver con el ciclo síntoma→propuesta→aprobación de `donatrack-audit-and-fix-workflow` (el síntoma es "la skill X ya no refleja la consigna vigente", la propuesta es el ajuste concreto al texto de la skill).
5. Recién con eso resuelto, retomar la tarea que estaba en curso antes de detectar el cambio.

## Relación con las otras skills
- Reemplaza y profundiza la sección "Cuando el pedido es ambiguo" de `donatrack-requirements-guardrails` — esa skill señala las *red flags* de dominio a vigilar, esta define *cómo* se resuelve cualquier ambigüedad detectada (incluidas esas red flags).
- Se ejecuta **dentro** del paso 1-2 del ciclo de `donatrack-audit-and-fix-workflow` (describir síntoma / proponer solución): si la ambigüedad aparece ahí, se resuelve con esta skill antes de terminar de formular la propuesta técnica — no tiene sentido proponerle al usuario una solución construida sobre un supuesto no confirmado.

## Qué NO hacer
- No asumir un valor "razonable" y seguir de largo sin preguntar, aunque parezca trivial — la regla acordada es siempre preguntar, no es una skill de "decidir con criterio propio".
- No preguntarle al usuario algo que ya está resuelto en `decisiones.md` o en el documento de requerimientos — es responsabilidad del agente revisarlos primero, no del usuario acordarse de haberlo dicho antes.
- No seguir trabajando sobre una tarea en curso si se detectó que el PDF cambió — el chequeo de compatibilidad va primero.
