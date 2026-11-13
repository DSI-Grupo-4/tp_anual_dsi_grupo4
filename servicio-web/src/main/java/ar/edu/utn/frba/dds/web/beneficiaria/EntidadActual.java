package ar.edu.utn.frba.dds.web.beneficiaria;

/**
 * La entidad beneficiaria con la que opera la persona que está usando el portal.
 *
 * Todas las pantallas de /beneficiaria/** piden el id por acá y solo muestran o modifican recursos de esa entidad.
 * EntidadActualConfigurada toma una entidad vinculada de la sesión OIDC;
 * el ID configurado queda disponible únicamente para pruebas sin autenticación.
 */
public interface EntidadActual {

    Long id();
}
