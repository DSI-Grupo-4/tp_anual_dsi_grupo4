package ar.edu.utn.frba.dds.logistica.dto;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Camion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoCamion;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/** Última ubicación informada de un camión. {@code actualizadaEn} va en UTC. */
@Getter
@AllArgsConstructor
public class UbicacionCamionDTO {

    private Integer idCamion;
    private String patente;
    private EstadoCamion estadoCamion;
    private Double latitud;
    private Double longitud;
    private LocalDateTime actualizadaEn;
    /** true si la posición la generó el simulador de la demo en vez del chofer. */
    private boolean simulada;

    public static UbicacionCamionDTO de(Camion camion) {
        return new UbicacionCamionDTO(camion.getIdCamion(), camion.getPatente(), camion.getEstadoCamion(),
                camion.getLatitud(), camion.getLongitud(), camion.getUbicacionActualizadaEn(),
                Boolean.TRUE.equals(camion.getUbicacionSimulada()));
    }
}
