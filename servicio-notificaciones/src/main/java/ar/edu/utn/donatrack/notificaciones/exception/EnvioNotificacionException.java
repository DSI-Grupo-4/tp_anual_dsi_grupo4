package ar.edu.utn.donatrack.notificaciones.exception;

/**
 * Se lanza cuando ocurre un error al intentar procesar el envío de
 * una notificación (por ejemplo, si en el futuro la integración real
 * con el proveedor externo falla).
 */
public class EnvioNotificacionException extends RuntimeException {
    public EnvioNotificacionException(String mensaje) {
        super(mensaje);
    }

    public EnvioNotificacionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
