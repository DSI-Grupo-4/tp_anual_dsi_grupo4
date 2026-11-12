# Persona 5 — parte 3: ubicación del camión, simulador y mapas

Se copia encima de la rama `feat/servicio-web` (después de las partes 1 y 2). Descomprimir en la raíz del repo.

## Qué incluye

**Logística (punto 1 y 2)**
- `Camion`: guarda su última ubicación (`latitud`, `longitud`, `ubicacionActualizadaEn` en UTC, `ubicacionSimulada`).
- `Direccion`: suma `latitud` y `longitud` opcionales (las usará el simulador y el mapa cuando las sedes las tengan).
- Endpoints nuevos: `PUT /api/camiones/{id}/ubicacion`, `GET /api/camiones/{id}/ubicacion`, `GET /api/camiones/ubicaciones`
  y `POST /api/rutas/{id}/simular-avance` (solo demo).
- `application.properties`: 5 propiedades `logistica.simulador.*` con valores por defecto.
- `diagramas/der/logistica.txt` actualizado con los campos nuevos.
- Tests: geografía, ubicación del camión, servicio de ubicaciones y simulador.

**Cliente web (punto 3)**
- Mapa con el camión en `/admin/seguimiento` (todas las rutas en curso), `/admin/seguimiento/{id}` (camión y paradas con coordenadas)
  y `/beneficiaria/seguimiento` (solo el camión que le lleva algo a la entidad). Se refrescan solos cada 10 a 15 segundos.
- Botón «Simular avance del camión» en el detalle de una ruta en curso.
- `MarcadoresSeguimiento.paraDonaciones(...)`: el helper para que la Persona 4 arme el mapa del donante (ver `contrato-ubicacion-para-persona4.md`).

**Documentos:** `decisiones-persona5-borrador.md` (cuatro entradas para `decisiones.md`) y `contrato-ubicacion-para-persona4.md`.

## Cómo probar la demostración completa

1. Levantar todo (`run-servicios`) y tener una entidad con el id que usa el portal (`BENEFICIARIA_ENTIDAD_ID`, por defecto 1).
2. Cargar un camión, un chofer y una donación asignada a esa entidad; enviar el lote y apretar «Planificar ahora» en `/admin/seguimiento`.
3. Abrir la ruta y apretar «Iniciar ruta»: las entregas pasan a «En traslado».
4. Apretar «Simular avance del camión» varias veces: el camión aparece en el mapa y se acerca a la parada.
5. Ver el mismo camión en `/beneficiaria/seguimiento`.

## Cosas para saber

- Los puntos de ejemplo del simulador **no son reales**: el marcador dice «Ubicación simulada». Con
  `logistica.simulador.usar-puntos-de-ejemplo=false` el paso se rechaza si la parada no tiene coordenadas.
- Un camión que nunca informó su ubicación no aparece en el mapa: no se inventan posiciones.
- El filtro por entidad y por donante se hace en el cliente web. Los datos de `GET /api/rutas` siguen abiertos hasta que la
  Persona 2 proteja las APIs.
- Archivos compartidos modificados: `RutasClient.java`, `CamionApi.java`, `RutaApi.java` y `messages-logistica.properties` (míos);
  `Camion.java`, `Direccion.java` y `application.properties` de Logística (si otra persona los tocó, quedarse con ambos cambios).
