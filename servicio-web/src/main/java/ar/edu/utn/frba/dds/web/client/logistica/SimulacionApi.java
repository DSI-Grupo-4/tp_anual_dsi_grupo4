package ar.edu.utn.frba.dds.web.client.logistica;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

/** Resultado de un paso del simulador de ubicación (POST /api/rutas/{id}/simular-avance). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SimulacionApi(
        UbicacionApi ubicacion,
        Integer numeroParadaDestino,
        double distanciaRestanteKm,
        boolean llegoAlDestino,
        boolean destinoDeEjemplo) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UbicacionApi(
            Integer idCamion,
            String patente,
            Double latitud,
            Double longitud,
            LocalDateTime actualizadaEn,
            boolean simulada) {
    }
}
