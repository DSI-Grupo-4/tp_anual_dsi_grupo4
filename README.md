# Trabajo Práctico Anual Diseño de Sistemas de Información Grupo 4


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
├── README.md
├── assets_md
│   └── Diagrama_de_Despliegue_Inicial.png
├── cli.py
├── diagramas
│   ├── DCU.png
│   ├── DDC.png
│   └── Diagrama Secuencia.jpeg
├── docker-compose.integration.yml
├── endpoints.md
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
├── run-servicios.sh
├── servicio-donaciones
│   ├── pom.xml
│   └── src
│       ├── data
│       └── main
├── servicio-incentivos
│   ├── Makefile
│   ├── README.md
│   ├── docker-compose.yml
│   ├── pom.xml
│   ├── src
│   │   └── main
│   └── workflows
│       ├── Workflow-Ranking.json
│       ├── servicio-incentivos-difusion.json
│       ├── servicio-incentivos.json
│       └── servicio-insignias.json
├── servicio-logistica
│   ├── pom.xml
│   └── src
│       └── main
└── servicio-notificaciones
    ├── pom.xml
    └── src
        └── main

22 directories, 43 files
```
<!-- TREE_END -->


## Diagrama de Despliegue inicial
![Diagrama de Despliegue Inicial](./assets_md/Diagrama_de_Despliegue_Inicial.png)

## Integracion y despliegue

- Servicio de Logistica desplegado: https://tp-anual-dsi-grupo4-1.onrender.com
- RabbitMQ local: `docker compose -f docker-compose.integration.yml up -d`
- Consola de RabbitMQ: http://localhost:15672 (`guest` / `guest`)
- El broker alterna cada envio entre Logistica local (`http://localhost:8083`) y el despliegue web. Si el elegido no responde, prueba el otro en ese mismo envio.
- Arquitectura, decisiones, diagramas editables y pruebas: [docs/entrega4-integracion.md](docs/entrega4-integracion.md)

