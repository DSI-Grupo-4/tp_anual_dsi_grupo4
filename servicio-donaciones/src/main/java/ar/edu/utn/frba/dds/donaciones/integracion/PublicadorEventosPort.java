package ar.edu.utn.frba.dds.donaciones.integracion;

/**
 * Puerto hacia la cola de mensajes del Servicio de Notificaciones. El
 * contrato se definió antes de tener la implementación real (RabbitMQ)
 * detrás, para que Donaciones pudiera desplegarse y probarse de forma
 * aislada, con un adapter no-op registrado mientras tanto. Cuando se
 * implementó la cola real, esa implementación se registró bajo el mismo
 * puerto sin tocar quién lo invoca.
 */
public interface PublicadorEventosPort {
    void publicar(String tipoEvento, Object payload);
    default void publicar(String tipoEvento, Object payload, ar.edu.utn.frba.dds.donaciones.dto.CambioEstadoDTO contexto) {
        publicar(tipoEvento, payload);
    }
}
