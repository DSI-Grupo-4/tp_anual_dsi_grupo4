---
name: donatrack-logistics-broker
description: Diseño e implementación del broker de integración entre el Servicio de Donaciones y el Servicio de Logística de DonaTrack, incluyendo selección entre múltiples proveedores de logística y el contrato de callback de planificación de rutas. Usar al implementar el broker, al agregar un nuevo proveedor de logística, al diseñar el endpoint de callback, o al aplicar resiliencia (timeouts, circuit breaker) sobre esa integración.
---

# Broker de Integración con Logística — DonaTrack

## Qué pide la consigna, literal
Un broker que permita **seleccionar entre más de un servicio de logística disponible** (el propio construido por el equipo + al menos otro potencial equivalente). No es solo "llamar a la API de Logística": es una capa de abstracción que oculte a Donaciones cuál proveedor concreto está respondiendo.

## Patrón recomendado
- Interfaz de dominio en el Servicio de Donaciones, ej. `ProveedorLogisticaClient`, con el contrato mínimo: enviar donaciones en estado `Asignación realizada` + camiones disponibles, recibir confirmación de la solicitud.
- Una implementación por proveedor (`LogisticaPropiaClient`, `LogisticaExternaClient`, ...) — patrón **Strategy** o **Adapter** según si los proveedores exponen la misma forma de API o hay que traducir contratos distintos. Si los proveedores tienen contratos de API diferentes, es Adapter; si comparten contrato y solo cambia el endpoint/credenciales, es Strategy.
- Un `ProveedorLogisticaBroker` (o `Router`) que decide **cuál** implementación usar en cada ejecución — por configuración, por disponibilidad (health check), o por regla de negocio a definir y documentar. No hardcodear "siempre el propio servicio" salvo que se documente explícitamente esa decisión como el valor por defecto configurable.

## Restricciones heredadas de Entrega 3 (siguen vigentes)
- El proceso de planificación de rutas corre en **horarios de baja carga**.
- Cada ejecución al proveedor procesa **como máximo 100 donaciones** — el broker (o quien lo invoque) debe loteor (`chunk`) el conjunto de donaciones en lotes de ≤100 antes de enviarlos, sin importar cuál proveedor esté detrás.
- El resultado de la planificación llega por una **URL de callback** expuesta por Donaciones — el broker debe correlacionar la respuesta asíncrona con la solicitud original (ej. incluyendo un `solicitudId` en el pedido y esperándolo de vuelta en el callback).
- Logística no debe ser invocada síncronamente esperando el resultado final de ruteo si ese resultado depende de un componente externo que responde por callback — modelar como fire-and-callback, no como llamada bloqueante que espera la ruta completa.

## Resiliencia sobre la llamada al proveedor
- Timeout explícito + circuit breaker (`Resilience4j` con Spring Boot) por proveedor, para que un proveedor caído no bloquee la asignación de donaciones que podrían ir a otro proveedor disponible.
- Si todos los proveedores configurados fallan, la donación debe quedar en un estado consistente con la máquina de estados ya definida (permanece en el estado que tenía, no se inventa un estado nuevo no contemplado en la consigna) y debe quedar logueado/observable para revisión manual.

## Qué NO hacer
- No meter lógica de selección de proveedor en el controller REST — vive en el broker.
- No acoplar el modelo de dominio de Donaciones a los DTOs específicos de un proveedor — mapear en el Adapter correspondiente.
- No usar este broker como excusa para que Donaciones llame directamente a endpoints internos de Logística sin pasar por el contrato público (rompe el desacoplamiento que pide la arquitectura distribuida).

## Al documentar (Diagrama de Componentes / Justificación de Diseño)
Mostrar explícitamente el broker como componente separado, con las implementaciones de proveedor como componentes intercambiables detrás de la interfaz — es el diagrama que evidencia que el requerimiento "más de 1 proveedor" está realmente resuelto en la arquitectura y no solo en el código.
