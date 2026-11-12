package ar.edu.utn.frba.dds.web.client.logistica;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Foto ya guardada en Logística: su id y la URL pública con la que se confirma la recepción. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FotoSubidaApi(String id, String url) {
}
