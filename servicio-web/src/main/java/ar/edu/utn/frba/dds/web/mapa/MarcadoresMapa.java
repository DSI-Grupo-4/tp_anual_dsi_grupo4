package ar.edu.utn.frba.dds.web.mapa;

import java.util.List;

/**
 * Marcadores de un mapa y de dónde salen, para que la vista avise si son de ejemplo
 * o si todavía no hay datos de ubicación.
 */
public record MarcadoresMapa(List<Marcador> marcadores, Origen origen) {

    public enum Origen {
        /** Datos reales de las APIs. */
        REAL,
        /** Datos inventados para desarrollo (perfil dev). La vista lo dice. */
        EJEMPLO,
        /** Las APIs todavía no exponen coordenadas (D-042 pendiente). */
        SIN_COORDENADAS
    }
}
