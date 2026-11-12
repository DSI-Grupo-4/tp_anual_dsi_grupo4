# Persona 5 — parte 5: entregas de la administración (punto 8)

Se copia encima de la rama `feat/servicio-web` (después de las partes 1 a 4). Descomprimir en la raíz del repo.

## Qué incluye

**Logística**
- `POST .../paradas/{id}/no-recibida` y `POST .../paradas/{id}/incidente` ahora exigen la ruta en curso y solo afectan a las entregas que
  seguían en traslado (antes marcaban todas las de la parada, incluso las ya recibidas). Es un cambio de comportamiento: ver
  `decisiones-persona5-borrador-parte5.md`.
- `Parada` suma `marcarNoRecibidasLasEnTraslado` y `marcarFalladasLasEnTraslado`. Los métodos anteriores no se tocaron.

**Cliente web (administración)**
- `/admin/seguimiento/entregas`: listado de entregas con cifras, filtro por estado, entidad y motivo de las que fallaron.
  Desde ahí, «Reingresar al depósito» (con confirmación) para las `NO_RECIBIDA` y `FALLIDA`.
- Detalle de una ruta en curso: por cada parada con entregas en traslado, «Registrar no recibida» y «Registrar incidente»
  (cada uno con su diálogo y un motivo obligatorio de hasta 300 caracteres).
- `/admin/seguimiento` suma el botón «Ver entregas».

## Cómo probarlo

1. Con una ruta iniciada (ver `PERSONA5-parte3.md`), abrir su detalle y apretar «Registrar incidente» en una parada.
2. Ir a «Ver entregas»: la entrega aparece como «Fallida» con su motivo. Apretar «Reingresar al depósito».
3. Apretar «Planificar ahora»: la entrega vuelve a una ruta nueva. Donaciones la vuelve a ver «En depósito» cuando consulta los eventos.

## Cosas para saber

- Si una llamada a `no-recibida` o `incidente` sobre una ruta sin iniciar «funcionaba» en la colección de Postman, ahora responde 409.
- Los eventos que generan estas acciones los consume Donaciones: conviene probar el flujo completo con la persona 3.
- Archivos compartidos modificados: `ParadaController.java` y `Parada.java` de Logística; `RutasClient.java`, `RutaApi.java`, `SeguimientoService.java`,
  `SeguimientoAdminController.java` y los textos `messages-logistica.properties` del cliente web (míos).
