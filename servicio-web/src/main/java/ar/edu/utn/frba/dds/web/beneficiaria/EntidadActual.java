package ar.edu.utn.frba.dds.web.beneficiaria;

/**
 * La entidad beneficiaria con la que opera la persona que está usando el portal.
 *
 * Todas las pantallas de /beneficiaria/** piden el id por acá y solo muestran o modifican recursos de esa entidad.
 * Hoy la implementación (EntidadActualConfigurada) lee el id de la configuración. Cuando exista la autenticación
 * (Persona 2), hay que reemplazarla por una que lo tome de la cuenta del usuario, sin tocar ninguna pantalla.
 */
public interface EntidadActual {

    Long id();
}
