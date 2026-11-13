package ar.edu.utn.frba.dds.web.donante;

/**
 * Qué donante está usando el portal /donante/**.
 *
 * {@link DonanteActualProvisorio} obtiene el ID de la sesión OIDC y conserva
 * el ID de pruebas únicamente para el modo sin autenticación.
 */
@FunctionalInterface
public interface DonanteActual {

    /** ID del Donante en el servicio de Donaciones. */
    long id();
}
