package ar.edu.utn.frba.dds.web.donante;

/**
 * Qué donante está usando el portal /donante/**.
 *
 * Punto de extensión para la Persona 2, como PropagadorCredenciales: cuando exista
 * autenticación, se declara un @Component @Primary que obtenga el ID del Donante
 * vinculado al usuario autenticado (por ejemplo, de un claim del token).
 * Mientras tanto se usa {@link DonanteActualProvisorio}.
 */
@FunctionalInterface
public interface DonanteActual {

    /** ID del Donante en el servicio de Donaciones. */
    long id();
}
