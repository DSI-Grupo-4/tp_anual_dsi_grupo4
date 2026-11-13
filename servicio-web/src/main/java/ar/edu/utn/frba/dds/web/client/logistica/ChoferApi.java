package ar.edu.utn.frba.dds.web.client.logistica;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Chofer tal como lo devuelve Logística (ChoferDTO). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChoferApi(Integer idChofer, String nombre, Integer dni, Boolean habilitado) {
}
