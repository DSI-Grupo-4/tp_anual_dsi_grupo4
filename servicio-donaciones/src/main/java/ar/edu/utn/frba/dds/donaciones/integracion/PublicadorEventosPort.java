package ar.edu.utn.frba.dds.donaciones.integracion;

/**
 * Puerto hacia la futura cola de mensajes del Servicio de Notificaciones
 * (RF-3, Entrega 4 — ver decisiones.md). Todavía no hay una implementación
 * real (RabbitMQ) detrás: el contrato se define ahora para que Donaciones
 * pueda desplegarse y probarse de forma aislada (Etapa 4), con un adapter
 * no-op registrado mientras tanto. Cuando se implemente la cola real, esa
 * implementación se registra bajo otro perfil sin tocar quién la invoca.
 */
public interface PublicadorEventosPort {
    void publicar(String tipoEvento, Object payload);
}
