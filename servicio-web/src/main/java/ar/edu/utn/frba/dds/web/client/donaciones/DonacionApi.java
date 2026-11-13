package ar.edu.utn.frba.dds.web.client.donaciones;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/** Donación tal como la devuelve el servicio de Donaciones (DonacionDTO). Solo los campos que usa el cliente web. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DonacionApi(
        Long id,
        String descripcionItem,
        BigDecimal cantidadAsignada,
        String unidadMedida,
        String categoria,
        String subcategoria,
        String estadoActual,
        Long entidadBeneficiariaId) {
}
