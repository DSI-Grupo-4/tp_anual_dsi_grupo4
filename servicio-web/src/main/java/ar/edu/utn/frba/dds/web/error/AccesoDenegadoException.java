package ar.edu.utn.frba.dds.web.error;

/** La API respondió 403: el usuario no tiene permiso sobre ese recurso. */
public class AccesoDenegadoException extends BackendException {

    public AccesoDenegadoException(ServicioBackend servicio, String mensaje) {
        super(servicio, 403, mensaje, null);
    }
}
