package ar.edu.utn.frba.dds.web.error;

/**
 * Error al llamar a una API de DonaTrack. Las subclases indican qué hacer en la vista:
 * <ul>
 *   <li>{@link BackendNoDisponibleException}: el servicio no respondió → página 503.</li>
 *   <li>{@link RecursoNoEncontradoException}: 404 del backend → página 404.</li>
 *   <li>{@link BackendRechazoException}: 400/409/413/422 → el controller lo muestra en el formulario o como toast.</li>
 *   <li>Cualquier otro → página 500.</li>
 * </ul>
 */
public class BackendException extends RuntimeException {

    private final ServicioBackend servicio;
    private final int status;

    public BackendException(ServicioBackend servicio, int status, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.servicio = servicio;
        this.status = status;
    }

    public ServicioBackend getServicio() {
        return servicio;
    }

    /** Status HTTP del backend, o 0 si no hubo respuesta. */
    public int getStatus() {
        return status;
    }
}
