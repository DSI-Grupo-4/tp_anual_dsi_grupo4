package ar.edu.utn.frba.dds.donaciones.dto;

import java.time.LocalDateTime;

/** Ubicación de un camión que lleva donaciones de quien consulta. Sin el resto de la ruta. */
public record CamionEnCursoDTO(
        String patente,
        Double latitud,
        Double longitud,
        LocalDateTime ubicacionActualizadaEn,
        boolean ubicacionSimulada,
        int donacionesEnTraslado) {
}
