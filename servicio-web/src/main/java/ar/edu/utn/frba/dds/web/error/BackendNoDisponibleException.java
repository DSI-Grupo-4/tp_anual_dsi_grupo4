package ar.edu.utn.frba.dds.web.error;

/** El servicio no respondió: conexión rechazada, timeout o 502/503/504. */
public class BackendNoDisponibleException extends BackendException {

    public BackendNoDisponibleException(ServicioBackend servicio, int status, Throwable causa) {
        super(servicio, status, "El servicio de " + servicio.getNombreVisible() + " no está disponible", causa);
    }
}
