# Flujo de notificaciones

Donaciones e Incentivos guardan solicitudes de aviso en una bandeja local y las publican en RabbitMQ en segundo plano. Logística registra eventos y Donaciones los consulta; Logística no invoca Notificaciones.

| Evento | Productor | Destinatarios |
| --- | --- | --- |
| BIENVENIDA_DONANTE | Donaciones, alta manual o primera importación | EMAIL del donante, aunque prefiera otro medio |
| INACTIVIDAD_20_DIAS | Donaciones, revisión diaria a las 04:00 | Contacto preferido del donante, después de más de 20 días |
| DONACION_ASIGNADA | Donaciones, confirmación de asignación | Entidad y donante |
| RUTA_PLANIFICADA | Donaciones, evento de Logística | Entidad y donante |
| RUTA_INICIADA | Donaciones, evento de Logística | Entidad y donante; incluye enlace interno de seguimiento |
| ENTREGA_CONFIRMADA | Donaciones, evento de Logística | Entidad y donante; comprobante con fecha/hora de recepción y patente |
| ENTREGA_FALLIDA / ENTREGA_NO_RECIBIDA | Donaciones, evento de Logística | Entidad, donante y administradores configurados |
| Cambio manual a VENCIDA | Donaciones | Donante, entidad si estaba asociada y administradores |
| MISION_COMPLETADA / CAMBIO_CATEGORIA | Incentivos | Contacto preferido del donante |

## Configuración y pruebas en Swagger

Desde la raíz del repositorio, con Docker disponible (`run-servicios.ps1` levanta RabbitMQ solo, junto con el resto de la infraestructura):

```powershell
$env:NOTIFICACIONES_ADMIN_EMAILS = "administrador@example.org"
$env:LOGISTICA_PUBLIC_BASE_URL = "http://localhost:8083"
.\run-servicios.ps1
```

Reemplazar el correo de ejemplo por los destinatarios de prueba. Se admite una lista separada por comas. Sin configurar administradores no se inventa ningún destinatario. Para destinatarios fuera de esta máquina, LOGISTICA_PUBLIC_BASE_URL debe ser una URL accesible del servicio desplegado.

1. Crear un donante humano/jurídico usando el ejemplo de Swagger: incluye mediosContacto, EMAIL obligatorio y exactamente un preferido. La bienvenida se encola para EMAIL.
2. Crear una entidad con personaJuridica.mediosContacto; crear su necesidad y una donación con el ID real del donante.
3. Ejecutar candidatas y confirmar asignación. Se generan avisos para ambas partes.
4. Planificar la ruta en Logística y comenzar el recorrido. Esperar la consulta automática (60 segundos) o ejecutar POST /api/logistica/consumir-eventos en Donaciones.
5. Seguir el enlace /api/rutas/{id}/seguimiento. Muestra un esquema de paradas y sus estados actuales, actualizado cada 15 segundos, sin servicios externos ni ubicación GPS. **No es un mapa geográfico ni implementa telemetría en tiempo real del camión**; eso requiere coordenadas y un componente cartográfico autorizado.
6. Confirmar recepción o registrar fallo en Logística y volver a consumir eventos. Consultar GET /api/notificaciones en el puerto 8082: los avisos procesados quedan COMPLETADA. SMS, EMAIL y WHATSAPP siguen simulados según el alcance del TP.
7. Para registrar una interacción del usuario (por ejemplo, al iniciar sesión), invocar POST /api/donantes/{id}/interacciones. También renuevan actividad las ediciones y cargas de donaciones. Las consultas administrativas no se interpretan como actividad del donante.
8. Incentivos recibe la actividad al crear la donación y una actualización al entregarla. donacionId identifica ambas fases y evita duplicar bienes, solicitudes y progreso. La confirmación de entrega actualiza misiones de donaciones exitosas; las otras misiones se procesan en el alta. Los cambios de contacto se sincronizan en cada actividad recibida.

## Reintentos y alcance de la persistencia

- Las bandejas de avisos de los productores, la bandeja HTTP hacia Incentivos y el historial/deduplicación de Notificaciones se guardan debajo de .datos/, ignorado por Git. No borrar ese directorio si se quieren conservar pendientes e historial.
- RabbitMQ debe confirmar tanto recepción como enrutamiento antes de borrar un aviso pendiente. Si el broker o el consumidor no está disponible, la operación de dominio no espera la conexión a RabbitMQ; el trabajador vuelve a intentar. La llamada HTTP a Incentivos también se realiza en segundo plano.
- Un evento de Logística sólo se confirma después de procesarlo correctamente y guardar los avisos. Ante error queda disponible para la siguiente consulta. En la misma ejecución se reconocen eventos ya aplicados y no se repite la acreditación de necesidades.
- El consumidor reintenta hasta tres veces. Hay una cola durable de fallidos configurada: donatrack.notificaciones.fallidas. Consultarla mediante RabbitMQ Management en localhost:15672.
- Notificaciones conserva las solicitudes completadas y la deduplicación por servicioOrigen + eventoId tras reiniciar. Los productores de Donaciones mantienen identificadores estables por evento y destinatario.
- Esto no implementa la persistencia completa del dominio del TP: donantes, donaciones, necesidades y progreso de Incentivos siguen en memoria. La deduplicación de actividades de Incentivos y eventos aplicados de Donaciones dura mientras esos dominios estén cargados. No se garantiza una transacción atómica entre el dominio en memoria y los archivos pendientes. La integración entrega al menos una vez; los proveedores reales futuros deberán aceptar una clave idempotente para cubrir una caída justo después del envío externo.

## Impacto en el diagrama de clases

No se agregaron clases de dominio. Las nuevas clases técnicas son **BandejaNotificaciones** en el paquete config de Donaciones y de Incentivos: contienen almacenamiento de solicitudes, publicación confirmada y reintentos. Se relacionan con el publicador/cliente existente y RabbitTemplate. No representan nuevas entidades ni cambian relaciones del dominio.

Cambios en clases existentes:

| Clase | Cambio |
| --- | --- |
| Entrega (Logística) | Agrega fechaHoraEntrega: LocalDateTime y seguimientoUrl: String. |
| DatosDonacion (Incentivos) | Agrega donacionId: Long para reconocer alta/confirmación de una misma donación. |
| Donante (Donaciones) | Conserva ultimaActividad y el indicador de aviso; corrige el límite a más de 20 días. |
| Donante (Incentivos) | Actualiza su contacto y reconoce donaciones identificadas sin agregar una entidad de contacto. |
| Notificacion | Conserva sus campos y relaciones; el servicio ahora almacena su historial en disco. |
| DTO y servicios | Transportan datos de comprobante/seguimiento y registran interacciones; no agregan relaciones de dominio. |

Los atributos nuevos de Entrega están reflejados en LOGISTICA.drawio.xml y diagramas/SERVICIOS.json, conservando sus estilos. DatosDonacion no tenía una caja propia en el diagrama nativo de Incentivos; su donacionId se detalla aquí y el DER propuesto ya contenía donacion_id. No se agregó una caja nueva al diagrama. Las pruebas agregadas y los adaptadores técnicos no se presentan como entidades del diagrama de dominio.

## Verificación realizada

119 pruebas aprobadas y una integración RabbitMQ omitida por falta de Docker (120 casos en total). Compilación y empaquetado de los cuatro servicios correctos. Se verificaron el arranque y Swagger de los cuatro servicios, altas de donante y entidad con contactos, asignación, entrega, actualización de necesidades, actividad de Incentivos sin doble conteo y procesamiento/deduplicación de solicitudes por REST. Con RabbitMQ apagado el alta respondió en menos de un segundo y quedaron avisos pendientes. La entrega real productor → RabbitMQ → consumidor no pudo probarse en este entorno.


## Perfil de Incentivos disponible desde el alta

El alta manual de un donante en Donaciones ahora sincroniza inmediatamente `PUT /api/donantes/{id}/perfil` de Incentivos. La edición sincroniza nombre y contacto. La importación CSV deja los perfiles en la bandeja para procesarlos en segundo plano. Si Incentivos no responde, el perfil queda en disco y se reintenta cada cinco segundos.

Ya no se necesita registrar una donación para consultar `GET /api/donantes/{id}/misiones`, `/metricas` o `/insignias` en Incentivos: el perfil inicial tiene las misiones, progreso cero y ninguna donación ni insignia obtenida. Si un perfil anterior falta (o Incentivos fue reiniciado), se verifica el ID contra `GET /api/donantes/{id}` de Donaciones y se recupera identidad/contacto. Un ID que no existe en Donaciones mantiene el 404; si Donaciones no responde, la recuperación devuelve 503.

En entornos desplegados configurar `DONACIONES_BASE_URL` en Incentivos y `incentivos.base-url` en Donaciones. En local los valores por defecto son `http://localhost:8080` y `http://localhost:8081`. Se reutilizan `IncentivosClient`, `DonanteService`, `IncentivosController` y las clases de donante existentes: no hay nuevas clases de producción ni cambios en relaciones del diagrama.

Se comprobó con dos instancias reales: alta humana en Donaciones seguida inmediatamente de GET de misiones en Incentivos, con cinco misiones disponibles, cero solicitudes de donación y cero insignias. Las pruebas adicionales cubren sincronización de perfil, reintentos y recuperación de un donante anterior desde Donaciones.
