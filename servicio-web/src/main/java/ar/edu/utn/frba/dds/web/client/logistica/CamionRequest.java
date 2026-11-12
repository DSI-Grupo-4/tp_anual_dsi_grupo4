package ar.edu.utn.frba.dds.web.client.logistica;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Cuerpo para crear o actualizar un camión. Si estadoCamion es null no se envía y Logística no lo cambia. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CamionRequest(
        String patente,
        Integer capacidadVolumenM3,
        Integer alturaM,
        Integer capacidadCargaKg,
        String estadoCamion) {
}
