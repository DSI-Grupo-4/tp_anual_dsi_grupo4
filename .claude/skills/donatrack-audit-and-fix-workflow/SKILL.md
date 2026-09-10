---
name: donatrack-audit-and-fix-workflow
description: Flujo de trabajo obligatorio para retomar y corregir el repositorio DonaTrack ya en desarrollo. Usar SIEMPRE al iniciar trabajo sobre el repo (primera interacción de una sesión, o cuando no está claro el estado actual del código), y usar el ciclo síntoma→propuesta→aprobación en CADA corrección o refactor que se proponga, salvo que el usuario pida explícitamente un hotfix puntual. Aplica a todos los servicios de DonaTrack.
---

# Flujo de Auditoría y Corrección — DonaTrack

Este proyecto ya tiene desarrollo previo. El agente no debe asumir el estado del código ni empezar a escribir sobre lo primero que encuentre — primero se audita, después se corrige, y la corrección normal nunca es "directo al código" sin pasar por aprobación explícita.

## Fase 0 — Escaneo profundo del repositorio (una vez por sesión de trabajo, o cuando el estado del repo cambió por fuera del agente)
1. Relevar todas las ramas locales y remotas: `git fetch --all --prune` seguido de `git branch -a -v`.
2. Para cada rama candidata, determinar avance real, no solo la fecha del último commit:
   - `git log --oneline <rama> | wc -l` y `git log <rama> -1 --format=%cd` como señal de actividad.
   - Comparar contra `main`/`develop` con `git log main..<rama> --oneline` y `git log <rama>..main --oneline` para ver commits de adelanto/atraso en ambos sentidos.
   - Cruzar contra el checklist de entregables de `entrega4-requerimientos.md` (o el documento de la entrega vigente) y contra las skills de dominio (`donatrack-requirements-guardrails`, `donatrack-jpa-persistence`, `donatrack-async-notifications`, `donatrack-logistics-broker`) para estimar **cobertura funcional real**, no solo volumen de commits — una rama con menos commits pero que ya resolvió persistencia y broker es "más avanzada" que una con más commits pero solo cambios cosméticos.
3. Documentar el hallazgo antes de tocar nada: qué rama se eligió como base y por qué (criterio de avance real), qué se encontró implementado por servicio (Donaciones, Logística, Incentivos, Notificaciones), y qué falta o está roto respecto a la consigna.
4. Crear la rama de trabajo nueva **local** desde la rama elegida (`git checkout -b <rama-nueva> <rama-base>`). Esto es una operación local, permitida sin pedir permiso — ver `donatrack-git-workflow` para las restricciones de qué NO se puede hacer (push, PR, merge remoto) con esa rama.
5. Recién después de esto arranca el trabajo de refactor/fix propiamente dicho.

## Fase 1 — Ciclo estándar de corrección (aplica por defecto, siempre)
Para cada problema detectado durante la auditoría o durante el desarrollo normal, el agente sigue este ciclo y **no se salta pasos**:

1. **Describir el síntoma**: qué se observa, dónde (archivo/clase/endpoint/servicio), y evidencia concreta (stack trace, comportamiento incorrecto, requerimiento incumplido de la consigna, test que falla). No generalizar ("el módulo de donaciones tiene problemas") — apuntar al síntoma específico.
2. **Proponer una solución técnica**: en qué consiste el arreglo a nivel de diseño/arquitectura (patrón a aplicar, capa afectada, por qué esta solución y no otra alternativa razonable), sin todavía bajar a diff de código archivo por archivo. Si al formular la propuesta aparece algo ambiguo (un valor no definido, dos fuentes que parecen contradecirse, alcance poco claro), resolverlo primero con `donatrack-ambiguity-resolution` antes de terminar de presentar la propuesta — no construir la propuesta sobre un supuesto sin confirmar. Esta es también la instancia donde el usuario puede pedir una alternativa distinta.
3. **Esperar la aprobación explícita del usuario.** El agente no avanza a implementar hasta que el usuario diga algo equivalente a "procedé" / "dale" / "implementalo". Si el usuario responde con dudas, ajustes o pide otra alternativa, se vuelve al paso 2 con la propuesta revisada.
4. **Recién con la aprobación, generar el plan de implementación paso a paso**: lista ordenada de cambios por archivo (archivo → qué se modifica/crea/borra → por qué), incluyendo migraciones, tests a actualizar y diagramas/documentación a tocar si corresponde. Si el cambio agrega, modifica o borra un endpoint, el plan debe incluir explícitamente el paso de re-sincronizar la colección de Bruno del servicio afectado (ver `donatrack-api-docs-bruno`). Si el cambio toca clases de dominio, entidades JPA o migraciones de esquema, el plan debe incluir explícitamente actualizar el DDC y/o DER correspondiente en `diagramas/` (ver `donatrack-system-modeling`). Ninguno de estos dos pasos es opcional ni posterior, son parte del mismo plan. El plan se presenta antes o junto con la implementación, no reemplaza el detalle de qué se tocó.
5. Implementar siguiendo el plan. Si durante la implementación aparece un problema no contemplado en el plan que requiere una decisión de diseño nueva, se corta y se vuelve al paso 1 para ese sub-problema — no se decide sobre la marcha.

## Excepción — Hotfix explícito
Si el usuario pide explícitamente un "hotfix" (o dice literalmente que quiere que se arregle ya, sin pasar por propuesta/aprobación) sobre un problema puntual y ya identificado, el agente puede saltar los pasos 2-3 e ir directo a implementar — pero:
- Igual debe indicar brevemente qué va a cambiar y por qué antes de tocar el archivo (una o dos líneas, no el ciclo completo), para que quede trazabilidad de qué se hizo y no una caja negra.
- El hotfix debe estar acotado al problema puntual señalado — si el agente detecta que el hotfix requiere tocar más superficie de la pedida (otro servicio, otra capa), avisa y confirma antes de expandir el alcance, en vez de asumir que el permiso de hotfix se extiende a todo lo relacionado.
- Esta excepción es por pedido explícito y puntual, no un modo general — la próxima corrección vuelve al ciclo estándar salvo que se pida hotfix de nuevo.

## Qué NO hacer
- No arrancar a refactorizar código sin haber hecho la Fase 0 al menos una vez en la sesión.
- No implementar cambios de diseño (nueva clase, nuevo patrón, cambio de contrato de API, migración de esquema) sin haber pasado por el ciclo síntoma→propuesta→aprobación, salvo hotfix explícito.
- No agrupar varios problemas no relacionados en una sola propuesta/aprobación "para ir más rápido" — cada problema con causa y solución distinta se presenta por separado, aunque se implementen después en el mismo lote si el usuario lo aprueba así.
- No olvidar que crear la rama de trabajo es local: sigue vigente la prohibición de `donatrack-git-workflow` de hacer push/PR/merge al remoto sin que el usuario lo ejecute él mismo.
