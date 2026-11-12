package ar.edu.utn.frba.dds.web.error;

/**
 * La API rechazó el token (401) o no se pudo renovar con el refresh token:
 * la sesión de Keycloak terminó. La vista cierra la sesión local y vuelve al login.
 */
public class SesionVencidaException extends BackendException {

    public SesionVencidaException(ServicioBackend servicio, String mensaje, Throwable causa) {
        super(servicio, 401, mensaje, causa);
    }
}
