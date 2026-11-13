package ar.edu.utn.frba.dds.logistica.dto;

import java.time.LocalDateTime;

/** Camión en curso que lleva alguna de las donaciones pedidas. Sin las demás entregas de la ruta. */
public record CamionEnCursoDTO(
        String patente,
        Double latitud,
        Double longitud,
        LocalDateTime ubicacionActualizadaEn,
        boolean ubicacionSimulada,
        int donacionesEnTraslado) {
}
