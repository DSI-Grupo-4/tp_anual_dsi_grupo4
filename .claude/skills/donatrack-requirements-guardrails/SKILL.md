---
name: donatrack-requirements-guardrails
description: Verificación de alcance y trazabilidad contra la consigna oficial del TP DonaTrack (UTN K3002). Usar SIEMPRE antes de implementar, testear o revisar cualquier funcionalidad de los servicios de Donaciones, Logística, Incentivos o Notificaciones, y antes de dar por "completa" una entrega. También usar cuando el usuario pida evaluar si algo cumple los requerimientos, generar tests de aceptación, o cuando algo parezca ambiguo respecto al dominio (estados de donación, segmentación, algoritmos de asignación, misiones).
---

# Guardrails de Requerimientos — DonaTrack

El objetivo de esta skill es que el agente no invente comportamiento ni recorte alcance sin decirlo. El TP se evalúa por entregas incrementales: una decisión de una entrega temprana (ej. estados de donación) sigue siendo obligatoria en entregas posteriores aunque no se repita explícitamente.

## Antes de implementar algo nuevo
1. Ubicar el requerimiento en el documento `entrega4-requerimientos.md` (o el documento de la entrega correspondiente). Si no aparece explícito, buscar en las entregas previas — el dominio es acumulativo, no se resetea entre entregas.
2. Si la tarea toca el **Servicio de Donaciones**, chequear que no rompa:
   - La segmentación automática de una carga en donaciones independientes por subcategoría.
   - La distinción bienes perecederos (fecha de vencimiento) vs. bienes con estado nuevo/usado.
   - La máquina de estados: `En depósito → Asignación realizada → Lista para entregar → En traslado → Entregada`, con las ramas `Entrega fallida → En depósito` y `Vencida`.
3. Si la tarea toca **Logística**, recordar la restricción dura: **no debe invocar a Donaciones ni a Incentivos, ni comunicarse con Notificaciones** — solo dejar información disponible (vía API/eventos que otros consuman, no llamadas salientes propias hacia esos servicios).
4. Si la tarea toca **Incentivos**, no perder de vista: misiones secuenciales por categoría (Colaborador → Sostenedor → Transformador), pérdida de progreso en misiones tipo "Racha", y que el ranking mensual es independiente de la categoría.
5. Si la tarea toca **Notificaciones**, confirmar el medio (correo/SMS/WhatsApp) y que a partir de Entrega 4 la invocación desde los demás servicios es **asíncrona vía cola**, no llamada síncrona directa.

## Antes de marcar una entrega como terminada
Repasar el checklist de entregables formales de esa entrega (modelo de clases, diagramas, documento de justificación, implementación, despliegue) — un servicio que compila y responde 200 OK no es "completo" si falta la documentación o el diagrama actualizado que pide la consigna.

## Cuando el pedido es ambiguo
No rellenar huecos del dominio por conveniencia técnica. Toda ambigüedad (valores no definidos, requerimientos que parecen contradecirse, dudas sobre alcance) se resuelve siguiendo el proceso obligatorio de `donatrack-ambiguity-resolution` — esa skill define el orden de fuentes a revisar antes de preguntar, el formato de la bitácora `decisiones.md`, y qué hacer si el PDF de consigna cambió. No asumir un valor por conveniencia ni preguntar sin haber revisado primero esas fuentes.

## Al testear / generar casos de prueba
Preferir casos derivados de los ejemplos concretos que da el propio enunciado (p. ej. donación de Arcos Plateados, comedor "Escobar Sonrisas", escuela rural N°10) antes que inventar datos desde cero — mantiene coherencia con lo que la cátedra probablemente use para evaluar.

## Red flags que ameritan avisar al usuario en vez de seguir de largo
- Un cambio que requeriría que Logística llame a otro servicio de dominio.
- Persistencia que implique una base de datos separada por servicio (la consigna pide un único motor con un esquema por servicio).
- Notificaciones invocadas de forma síncrona desde un servicio de dominio en Entrega 4 en adelante.
- Documentación de entregables (diagramas, justificaciones) que se salta porque "ya se codeó la funcionalidad".
