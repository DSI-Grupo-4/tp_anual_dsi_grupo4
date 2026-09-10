---
name: donatrack-async-notifications
description: Diseño e implementación de la integración asíncrona entre los servicios de dominio (Donaciones, Logística, Incentivos) y el Servicio de Notificaciones de DonaTrack mediante cola de mensajes. Usar al implementar el envío de eventos de dominio hacia notificaciones, al configurar RabbitMQ/Kafka con Spring, o al diseñar contratos de eventos, reintentos y colas de error (DLQ).
---

# Mensajería Asíncrona — Notificaciones (DonaTrack)

## Por qué es asíncrono (no perder de vista al justificar diseño)
La consigna es explícita: la integración debe ser asíncrona **para que picos de carga o fallas transitorias en Notificaciones no afecten la disponibilidad de los servicios de dominio**. Cualquier implementación que bloquee la respuesta de un endpoint de Donaciones/Logística/Incentivos esperando que Notificaciones procese el mensaje está incumpliendo el requerimiento, aunque use una librería de colas por debajo.

## Elección de tecnología
- Spring Boot: **RabbitMQ** vía `spring-boot-starter-amqp` es la opción de menor fricción para un TP (exchange + colas por tipo de evento, DLQ nativa). Kafka es válido si el equipo ya lo maneja, pero agrega complejidad de particiones/consumer groups que la consigna no exige.
- Un exchange por servicio productor o un exchange tipo `topic` compartido con routing keys por tipo de evento (`donacion.asignada`, `mision.completada`, `entidad.donacion.asignada`, `donante.inactivo`, `categoria.cambiada`, `ruta.iniciada`, `entrega.exitosa`, `entrega.fallida`) — decidir y documentarlo, no improvisar el nombre de las colas por endpoint.

## Contrato del mensaje
Cada evento publicado debe incluir, como mínimo:
```json
{
  "tipoEvento": "DONACION_ASIGNADA",
  "timestamp": "2026-09-10T12:00:00Z",
  "destinatarioId": "...",
  "tipoDestinatario": "PERSONA_DONANTE | ENTIDAD_BENEFICIARIA",
  "medioPreferido": "EMAIL | SMS | WHATSAPP",
  "payload": { }
}
```
No enviar entidades JPA completas como payload — un DTO explícito por tipo de evento evita acoplar el contrato de mensajería al modelo de persistencia.

## Eventos obligatorios a cubrir (según consigna, Entregas 2 y 3)
1. Donante sin interacción > 20 días → recordatorio. (Donaciones)
2. Entidad beneficiaria: asignación de donación por necesidad recurrente/extraordinaria. (Donaciones)
3. Donante: su donación fue asignada. (Donaciones)
4. Donante: cumplió una misión / cambió de categoría. (Incentivos)
5. Inicio de ruta → notificar a entidades y donantes involucrados, con enlace al mapa de seguimiento. (originado en Logística, pero publicado por el servicio que corresponda dado que Logística no debe comunicarse directo con Notificaciones — ver más abajo)
6. Entrega exitosa / entrega no satisfactoria, con comprobante o motivo. (idem)

## Restricción dura: Logística no habla con Notificaciones
La consigna dice explícitamente que el servicio de logística no debe comunicarse con el servicio de notificaciones, y tampoco debe invocar a Donaciones/Incentivos — solo debe **dejar disponible la información**. Diseño recomendado:
- Logística expone/emite el resultado de sus operaciones (vía callback, API de consulta, o su propio evento) y es **Donaciones** (u otro componente que sí tiene permitido integrar con Notificaciones) quien, al enterarse del cambio, publica el evento correspondiente en la cola.
- No armar un atajo donde Logística publique directo en la cola de Notificaciones "porque es más simple" — es una restricción explícita del enunciado, no un detalle de implementación.

## Resiliencia
- Reintentos con backoff exponencial (`spring-retry` o política nativa de RabbitMQ) antes de mandar a Dead Letter Queue.
- El consumidor en Notificaciones debe ser idempotente (deduplicar por `eventoId`) — un reintento no debe mandar la notificación dos veces.
- El productor no debe fallar la operación de negocio si la publicación falla de forma transitoria: usar outbox pattern si el equipo tiene tiempo, o al menos loggear y reintentar sin bloquear la transacción principal — documentar la decisión tomada y su trade-off.

## Al escribir el documento de justificación de esta entrega
Explicar explícitamente el trade-off elegido: consistencia eventual entre "la donación cambió de estado" y "la notificación efectivamente llegó", y por qué es aceptable en este dominio.
