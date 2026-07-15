# n8n — Comandos

El dashboard/editor web de n8n está deshabilitado (`N8N_DISABLE_UI=true` en
`docker-compose.yml`); sólo quedan expuestos la REST API y los webhooks. El
workflow se importa y se activa automáticamente al levantar el contenedor
(y también vía `make deploy`/`make activate`), sin necesidad de abrir el editor.

| Comando | Descripción |
|---|---|
| `make up` | Levanta el contenedor (importa y activa el workflow al iniciar) |
| `make down` | Detiene el contenedor (el volumen no se elimina) |
| `make deploy` | Levanta el servicio, importa y activa el workflow desde `./workflows` |
| `make import` | Importa workflows sin reiniciar el contenedor |
| `make activate` | Activa todos los workflows importados |
| `make export-workflows` | Exporta los workflows activos hacia `./workflows` |
| `make save` | Exporta los workflows y los commitea |
 
