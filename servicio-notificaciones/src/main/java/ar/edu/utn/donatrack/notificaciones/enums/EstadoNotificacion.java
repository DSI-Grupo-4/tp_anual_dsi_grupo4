package ar.edu.utn.donatrack.notificaciones.enums;

/**
 * Estado de una notificación dentro de su ciclo de vida.
 * Corresponde al ENUM "EstadoNotificacion" del diagrama de clases
 * (Servicio de Notificaciones - Entrega 2).
 *
 * En esta entrega el envío a los medios externos se simula, por lo que
 * una notificación pasa de PENDIENTE a COMPLETADA inmediatamente después
 * de "enviarse". En próximas entregas, al integrar los servicios reales,
 * podría incorporarse un estado FALLIDA.
 */
public enum EstadoNotificacion {
    PENDIENTE,
    COMPLETADA
}
