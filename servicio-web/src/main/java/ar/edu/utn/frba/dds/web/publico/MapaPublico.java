package ar.edu.utn.frba.dds.web.publico;

import ar.edu.utn.frba.dds.web.mapa.MarcadoresMapa;

/**
 * Datos del mapa público de donaciones entregadas (requerimiento mínimo de UI n°2).
 * Las APIs todavía no tienen coordenadas (D-042): la implementación real se escribe
 * cuando la Persona 5 las exponga en Logística.
 */
public interface MapaPublico {

    MarcadoresMapa entregasRealizadas();
}
