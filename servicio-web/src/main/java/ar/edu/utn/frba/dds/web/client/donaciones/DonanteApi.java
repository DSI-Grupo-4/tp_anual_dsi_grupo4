package ar.edu.utn.frba.dds.web.client.donaciones;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Donante (DonanteDTO). Solo id y estado: el cliente web no lee datos personales para las vistas públicas. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DonanteApi(Long id, Boolean activo) {
}
