package ar.edu.utn.frba.dds.web.error;

/** El backend respondió 404 para el recurso pedido. */
public class RecursoNoEncontradoException extends BackendException {

    public RecursoNoEncontradoException(ServicioBackend servicio, String mensaje) {
        super(servicio, 404, mensaje, null);
    }
}
