# Persona 5 — entrega 1: portal de la entidad beneficiaria (parte 1)

Archivos nuevos y modificados de `servicio-web`, listos para copiar encima de la rama `feat/servicio-web`.
Descomprimir en la raíz del repo.

## Qué incluye

| Pantalla | Ruta | Estado |
|---|---|---|
| Panel de la entidad | `/beneficiaria` | Hecha |
| Necesidades: lista, alta, edición y baja (recurrentes y extraordinarias) | `/beneficiaria/necesidades[/nueva\|/{id}]` | Hecha |
| Donaciones asignadas, con filtro y detalle con historial | `/beneficiaria/donaciones[/{id}]` | Hecha |
| Confirmación de recepción con fotos | `/beneficiaria/donaciones/{id}` | Falta (parte 2) |
| Seguimiento | `/beneficiaria/seguimiento` | Falta (parte 3) |

## Archivos modificados (pueden tener conflicto con otras ramas: quedarse con ambos cambios)

- `client/donaciones/DonacionesClient.java`: se agregaron `donacion(id)` e `historial(id)` al final. Si la Persona 3 o 4 ya los agregó, quedarse con uno solo.
- `src/main/resources/application.properties`: `spring.messages.basename=messages,messages-beneficiaria` y la propiedad `donatrack.web.beneficiaria.entidad-id`.
- `src/test/.../navegacion/NavegacionWebTest.java`: ahora simula los clientes de Donaciones, porque el panel y las necesidades de la entidad dejaron de ser «en construcción».

## Piezas que hay que reemplazar al integrar la autenticación (Persona 2)

`beneficiaria/EntidadActual` es la interfaz que dice con qué entidad opera quien usa el portal. Hoy la implementa
`EntidadActualConfigurada`, que lee el id de `donatrack.web.beneficiaria.entidad-id` (por defecto 1). Con la autenticación
hay que borrar esa clase y aportar una que tome el id de la cuenta del usuario. Ninguna pantalla cambia.

## Cómo probarlo

1. Levantar Donaciones y tener una entidad con id 1 (o definir `BENEFICIARIA_ENTIDAD_ID`).
2. `mvn -pl servicio-web spring-boot:run -Dspring-boot.run.profiles=dev` y abrir `http://localhost:8085/beneficiaria`.
3. Tests: `mvn -pl servicio-web test`.

## Decisiones de esta parte

- Los textos están en `messages-beneficiaria.properties` y no en `messages.properties`, para no pisarse con las otras ramas.
- Una entidad solo ve y modifica lo suyo: el detalle de una donación de otra entidad responde 404, y toda llamada a Donaciones lleva el id de `EntidadActual`.
- Donaciones no filtra por entidad ni trae fechas, así que el panel filtra en el cliente y no muestra cifras «de este mes» del boceto.
- Urgentes: son las necesidades extraordinarias que todavía no se cubrieron.
