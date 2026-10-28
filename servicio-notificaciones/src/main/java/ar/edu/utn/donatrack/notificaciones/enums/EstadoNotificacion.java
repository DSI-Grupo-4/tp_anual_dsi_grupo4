package ar.edu.utn.donatrack.notificaciones.enums;

/**
 * Estado de una notificación dentro de su ciclo de vida.
 * Corresponde al ENUM "EstadoNotificacion" del diagrama de clases
 * del Servicio de Notificaciones.
 *
 * Por ahora el envío a los medios externos se simula, por lo que
 * una notificación pasa de PENDIENTE a COMPLETADA inmediatamente después
 * de "enviarse". Al integrar los servicios reales más adelante,
 * podría incorporarse un estado FALLIDA.
 */
public enum EstadoNotificacion {
    PENDIENTE,
    COMPLETADA
}
