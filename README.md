# Trabajo Práctico Anual Diseño de Sistemas de Información Grupo 8


## Integrantes:
- Annetta, Dante
- Basile, Bautista
- Bravo Arreyes, Matías
- Durruty, Tomás
- Farret, Felipe
- Fuentes, Agustina
- González, Paula Nicole
- Quenallata Cruz, Brandon Carlos

## Tree
<!-- TREE_START -->
```text
.
├── Dockerfile
├── GUIA-LEVANTAR-SERVICIOS.md
├── README.md
├── assets_md
│   └── Diagrama_de_Despliegue_Inicial.png
├── consigna
│   ├── DDS-TP-Anual-2026-CursoK3002 Entrega 4.pdf
│   └── entrega4-requerimientos.md
├── decisiones.md
├── diagramas
│   ├── SERVICIOS.json
│   └── ddc
│       ├── DONACIONES.drawio.xml
│       ├── INCENTIVOS.drawio.xml
│       ├── LOGISTICA.drawio.xml
│       └── NOTIFICACIONES.drawio.xml
├── docker-compose.integration.yml
├── mockups
│   ├── administrador
│   │   ├── asignar-donaciones-admin.png
│   │   ├── beneficiarias-admin.png
│   │   ├── dashboard-admin.png
│   │   ├── donantes-admin.png
│   │   ├── gestion-donaciones-admin.png
│   │   ├── reporte-y-estadisticas-admin.png
│   │   └── seguimiento-admin.png
│   ├── donante
│   │   ├── beneficiarios-donante.png
│   │   ├── dashboard-donante.png
│   │   ├── donaciones-donante.png
│   │   ├── incentivos-donante.png
│   │   └── seguimiento-donantes.png
│   ├── entidad-beneficiaria
│   │   ├── dashboard-beneficiaria.png
│   │   ├── donaciones-asignadas-beneficiaria.png
│   │   ├── necesidades-beneficiaria.png
│   │   └── seguimiento-beneficiaria.png
│   ├── login
│   │   ├── crear-cuenta-beneficiario.png
│   │   ├── crear-cuenta-donante.png
│   │   └── iniciar-sesion.png
│   └── principal.png
├── pom.xml
├── render.yaml
├── run-servicios.ps1
├── run-servicios.sh
├── servicio-donaciones
│   ├── pom.xml
│   └── src
│       ├── data
│       ├── main
│       └── test
├── servicio-incentivos
│   ├── Makefile
│   ├── README.md
│   ├── credentials.env
│   ├── docker-compose.yml
│   ├── pom.xml
│   ├── src
│   │   ├── main
│   │   └── test
│   └── workflows
│       └── servicio-incentivos-difusion.json
├── servicio-logistica
│   ├── pom.xml
│   └── src
│       ├── main
│       └── test
├── servicio-notificaciones
│   ├── pom.xml
│   └── src
│       ├── main
│       └── test
└── stop-servicios.ps1

28 directories, 47 files
```
<!-- TREE_END -->


## Diagrama de Despliegue inicial
![Diagrama de Despliegue Inicial](./assets_md/Diagrama_de_Despliegue_Inicial.png)

## Despliegue
Servicio de Logística: https://tp-anual-dsi-grupo4-1.onrender.com




## Pruebas rápidas con Swagger: categorías y segmentación

Desde PowerShell, en la raíz del proyecto:

```powershell
mvn test
.\run-servicios.ps1
```

Swagger: Donaciones `http://localhost:8080/swagger-ui.html`, Incentivos `http://localhost:8081/swagger-ui/index.html`, Notificaciones `http://localhost:8082/swagger-ui/index.html` y Logística `http://localhost:8083/swagger-ui/index.html`.

Los cuerpos de entrada tienen ejemplos completos en `@Schema`; los parámetros de URL también tienen ejemplos. Los IDs de ejemplo son `1`: reemplazarlos por los que devuelve cada alta. Las entidades se guardan en memoria y se pierden al reiniciar. Para probar eventos por cola debe estar RabbitMQ disponible; las pruebas Testcontainers se omiten si Docker no está disponible.

1. Crear un donante: `POST /api/donantes/humanos` o `/juridicos`. Guardar su ID.
2. Crear una entidad con dirección completa: `POST /api/entidades`. Guardar su ID.
3. Crear necesidades en `POST /api/entidades/{entidadId}/necesidades/extraordinarias` (SILLA/UNIDAD) y `/recurrentes` (ARROZ/KILOGRAMO). El ID de entidad se toma de la URL.
4. Crear la carga con `POST /api/donaciones`, cambiando `donanteId`. El ejemplo incluye sillas usadas y 2,5 kg de arroz, y genera dos donaciones. Duplicar el renglón de sillas con otra descripción/foto mantiene una sola donación de sillas que conserva ambos renglones.
5. Consultar `GET /api/donaciones/{id}` y `/{id}/candidatas`. Para simular un ítem sin registrarlo, usar `POST /api/asignaciones/candidatas` con el mismo formato que un ítem.
6. Editar en depósito con `PUT /api/donaciones/{id}` y `{ "items": [...] }`. El grupo debe compartir subcategoría, unidad, condición y vencimiento. No se permite editar después de asignar. La carga original y el historial se conservan.
7. Asignar con `POST /api/donaciones/{id}/asignar`. Peso y volumen corresponden al total del renglón, no a cada unidad; altura es la máxima del renglón. Deben estar completos antes de asignar.
8. Probar Logística mediante `POST /api/logistica/enviar-pendientes` con Logística levantada, luego planificar/iniciar/confirmar en sus endpoints y consumir eventos desde Donaciones. Para una prueba manual de estados, después de asignar usar PATCH `/{id}/estado` en orden: LISTA_PARA_ENTREGAR, EN_TRASLADO, ENTREGADA.
9. Al entregar se informa a Incentivos. Consultar sus métricas/misiones con el ID del donante. El endpoint de actividad también tiene un ejemplo independiente; ejecutarlo manualmente además de entregar registraría otra actividad.

Validaciones rápidas: `ALIMENTOS + SILLA`, una silla sin condición, arroz sin vencimiento, cantidad cero, media silla o una unidad incompatible devuelven 400. Una carga inválida no registra los renglones anteriores. Kilos y paquetes de un mismo alimento se segmentan por separado, sin conversiones implícitas. Fechas de vencimiento distintas y condiciones NUEVO/USADO también generan grupos separados. Los ejemplos de alimentos vencen en 2030; actualizar esa fecha si fuera necesario.

Para detener los servicios:

```powershell
.\stop-servicios.ps1
```

El diseño acordado y sus cambios están detallados en `decisiones.md`, D-016. El diagrama actualizado está en `diagramas/SERVICIOS.json` y el draw.io nativo en `diagramas/ddc/DONACIONES.drawio.xml`.
