<!--
BORRADOR para pegar en decisiones.md. La numeración se asigna al integrar (D-044 en adelante está libre en la rama
feat/servicio-web, pero las otras personas también agregan entradas). Formato: el de las entradas existentes.
Todas figuran como «propuesta» hasta que las confirme el equipo.
-->

## [D-???] Ubicación del camión: campos propios en Logística y consulta por REST
- **Fecha:** 2026-11-11
- **Servicio(s) afectado(s):** Logística (`Camion`, `Direccion`, endpoints nuevos), cliente web (mapas de seguimiento)
- **Entrega vigente al momento:** Entrega 6
- **Contexto:** El enunciado pide un mapa interactivo con la ubicación del camión y su última actualización. Hasta ahora el seguimiento de Logística era un HTML con paradas y estados, sin ubicación. Logística no puede invocar a otros servicios: la ubicación se guarda y se deja disponible para consulta.
- **Fuentes revisadas:** consigna de la Entrega 6 / `servicio-web/README.md` (tareas de la Persona 5 y D-042) / código de `servicio-logistica`.
- **Opciones consideradas:**
  - A) Una colección `ubicaciones` con el historial de posiciones.
  - B) Campos de última ubicación dentro del documento del camión.
  - C) Que Logística publique la ubicación como evento hacia otro servicio.
- **Decisión tomada:** Opción B. El camión guarda `latitud`, `longitud`, `ubicacionActualizadaEn` (UTC) y `ubicacionSimulada`. Endpoints: `PUT /api/camiones/{id}/ubicacion` (lo usa el chofer; la fecha la pone el servidor), `GET /api/camiones/{id}/ubicacion` y `GET /api/camiones/ubicaciones` (solo los camiones que ya informaron una). Las coordenadas se validan (latitud de -90 a 90, longitud de -180 a 180). La ubicación también viaja dentro de `camionAsociado` en `GET /api/rutas`. `Direccion` suma `latitud` y `longitud` opcionales.
- **Consecuencias:** Solo se conserva la última posición, sin historial: alcanza para ver dónde está el camión ahora; guardar el recorrido requeriría decidir cuánto tiempo se conserva. Un camión que nunca informó no aparece en el mapa: no se inventan posiciones.
- **Definida por:** propuesta — pendiente de confirmar por el equipo

## [D-???] Simulador de ubicación para la demostración
- **Fecha:** 2026-11-11
- **Servicio(s) afectado(s):** Logística (`SimuladorRutaService`, `POST /api/rutas/{id}/simular-avance`), cliente web (botón en el detalle de la ruta)
- **Entrega vigente al momento:** Entrega 6
- **Contexto:** En la defensa no hay un chofer real que informe su posición, y el README prevé «el chofer o un simulador».
- **Fuentes revisadas:** `servicio-web/README.md` / código de Logística.
- **Opciones consideradas:**
  - A) Un botón que mueve el camión un paso por cada clic.
  - B) Un proceso programado que lo mueve solo cada pocos segundos.
  - C) No simular y cargar las posiciones a mano con `PUT`.
- **Decisión tomada:** Opción A. Cada clic avanza el camión el 25 % de la distancia que le falta hasta la próxima parada con entregas en traslado, y al acercarse a menos de 150 m lo deja en la parada. La posición queda marcada como simulada y la interfaz lo dice. El simulador **no confirma entregas ni cambia estados**. Si la parada no tiene coordenadas usa un punto de ejemplo cercano al depósito (siempre el mismo para la misma entidad); esto se desactiva con `logistica.simulador.usar-puntos-de-ejemplo=false`, y entonces el paso se rechaza con un 409 que pide cargar las coordenadas de la sede.
- **Consecuencias:** Un botón es predecible: se controla el ritmo de la demostración. Los puntos de ejemplo no son reales: en el mapa solo se dibujan las paradas que tienen coordenadas reales, y el camión muestra «Ubicación simulada». Cuando las sedes tengan coordenadas, el mismo simulador las usa sin cambios.
- **Definida por:** propuesta — pendiente de confirmar por el equipo

## [D-???] Mapas de seguimiento: quién ve qué
- **Fecha:** 2026-11-11
- **Servicio(s) afectado(s):** cliente web (`/admin/seguimiento`, `/beneficiaria/seguimiento`, y `/donante/seguimiento` de la Persona 4)
- **Entrega vigente al momento:** Entrega 6
- **Contexto:** Las tres zonas usan el mismo componente de mapa pero no deben ver los mismos datos.
- **Fuentes revisadas:** consigna de la Entrega 6 (restringir a recursos propios) / D-042 / `servicio-web/README.md`.
- **Decisión tomada:**
  - **Administración:** todos los camiones de rutas en curso; en el detalle de una ruta, además, las paradas que tienen coordenadas.
  - **Entidad beneficiaria:** solo el camión de las rutas en curso que le llevan algo. No ve las otras paradas, ni sus direcciones, ni el chofer.
  - **Donante:** solo el camión de las rutas en curso que llevan alguna de sus donaciones (`MarcadoresSeguimiento.paraDonaciones`), sin contar las donaciones ajenas.
  - Cada mapa se refresca solo cada 10 a 15 segundos desde un endpoint propio de su zona.
- **Consecuencias:** El filtro se hace hoy en el cliente web, porque Logística no sabe quién es el usuario. Cuando la Persona 2 restrinja las APIs por usuario, hay que hacerlo también en Logística: el filtro del cliente web es una protección de la pantalla, no de los datos.
- **Definida por:** propuesta — pendiente de confirmar por el equipo

## [D-???] Fotos de entrega guardadas en MongoDB (GridFS) — a implementar
- **Fecha:** 2026-11-11
- **Servicio(s) afectado(s):** Logística (endpoint nuevo), cliente web (carga desde el navegador)
- **Entrega vigente al momento:** Entrega 6
- **Contexto:** `FotoEntrega` recibe hoy una URL, pero la entidad sube un archivo desde el navegador.
- **Opciones consideradas:** A) Guardar el archivo en MongoDB con GridFS y servirlo desde Logística. B) Guardarlo en el disco del cliente web. C) Un servicio externo de almacenamiento.
- **Decisión tomada:** Opción A, elegida por el equipo. Logística recibe el archivo (`POST /api/fotos`, multipart), lo guarda en GridFS y devuelve la URL con la que se arma `FotoEntrega`. Se validan el tipo y el tamaño.
- **Consecuencias:** Las fotos viven en la misma base que el resto de los datos y no se pierden al reiniciar el servicio, a diferencia del disco de Render. Las instancias que comparten la base comparten las fotos.
- **Definida por:** usuario (persona 5)
