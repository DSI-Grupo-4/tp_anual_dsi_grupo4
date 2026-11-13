package ar.edu.utn.frba.dds.web.client.logistica;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

/**
 * Camión tal como lo devuelve Logística (CamionDTO, o el camión embebido en una ruta).
 * La ubicación es la última que informó el chofer o el simulador; la fecha llega en UTC.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CamionApi(
        Integer idCamion,
        String patente,
        Integer capacidadVolumenM3,
        Integer alturaM,
        Integer capacidadCargaKg,
        String estadoCamion,
        Double latitud,
        Double longitud,
        LocalDateTime ubicacionActualizadaEn,
        Boolean ubicacionSimulada) {

    /** Camión sin ubicación (el que devuelve el ABM de camiones). */
    public CamionApi(Integer idCamion, String patente, Integer capacidadVolumenM3, Integer alturaM,
                     Integer capacidadCargaKg, String estadoCamion) {
        this(idCamion, patente, capacidadVolumenM3, alturaM, capacidadCargaKg, estadoCamion, null, null, null, null);
    }

    /** El administrador solo puede cambiar el estado entre DISPONIBLE y FUERA_DE_SERVICIO: los otros los maneja el planificador. */
    public boolean estadoEditable() {
        return "DISPONIBLE".equals(estadoCamion) || "FUERA_DE_SERVICIO".equals(estadoCamion);
    }

    public boolean tieneUbicacion() {
        return latitud != null && longitud != null;
    }

    public boolean simulada() {
        return Boolean.TRUE.equals(ubicacionSimulada);
    }
}
