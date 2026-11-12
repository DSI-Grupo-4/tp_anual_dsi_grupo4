package ar.edu.utn.frba.dds.web.client.logistica;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Camión tal como lo devuelve Logística (CamionDTO). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CamionApi(
        Integer idCamion,
        String patente,
        Integer capacidadVolumenM3,
        Integer alturaM,
        Integer capacidadCargaKg,
        String estadoCamion) {

    /** El administrador solo puede cambiar el estado entre DISPONIBLE y FUERA_DE_SERVICIO: los otros los maneja el planificador. */
    public boolean estadoEditable() {
        return "DISPONIBLE".equals(estadoCamion) || "FUERA_DE_SERVICIO".equals(estadoCamion);
    }
}
