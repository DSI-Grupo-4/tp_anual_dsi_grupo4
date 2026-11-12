# Persona 5 — parte 2: camiones y seguimiento de rutas

Se copia encima de la rama `feat/servicio-web` (después de la parte 1). Descomprimir en la raíz del repo.

## Qué incluye

| Pantalla | Ruta | Estado |
|---|---|---|
| ABM de camiones (lista con cifras, alta, edición, baja) | `/admin/camiones[/nuevo\|/{id}]` | Hecha |
| Seguimiento de rutas: lista con filtro y cifras, «Planificar ahora» | `/admin/seguimiento` | Hecha |
| Detalle de una ruta con sus paradas y entregas, «Iniciar ruta» | `/admin/seguimiento/{id}` | Hecha |
| Seguimiento de las entregas de la entidad | `/beneficiaria/seguimiento` | Hecha (sin mapa) |

## Archivos modificados

- `src/main/resources/application.properties`: el `basename` de mensajes suma `messages-logistica` (ya incluye `messages-beneficiaria` de la parte 1).

Todo lo demás es nuevo. Los textos están en `messages-logistica.properties`.

## Cosas para saber

- **Estado de un camión:** el administrador solo elige entre «Disponible» y «Fuera de servicio». ASIGNADO y EN_RUTA los maneja el planificador: si el camión está en uno de esos estados, el formulario no deja cambiarlo y el controlador no envía el estado aunque lo manden a mano.
- **Baja de un camión:** Logística rechaza la baja si tiene entregas activas o figura en rutas anteriores; el rechazo se muestra como toast con su mensaje.
- **Lo que ve la entidad:** solo su parada de cada ruta (número de parada, camión, entregas y fotos). Nunca las otras paradas, direcciones ni el chofer.
- **Fotos:** solo se enlazan las URL que empiezan con http:// o https://.
- **Nombres de entidades:** Logística guarda el id; el seguimiento de la administración los pide a Donaciones y, si no responde, muestra «Entidad N».
- **Pendiente en estas pantallas:** el mapa (parte de ubicación del camión) y la confirmación de recepción con fotos (parte 2 del plan: depende de dónde se guardan las fotos).
