package ar.edu.utn.frba.dds.web.client.logistica;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Respuesta de POST /api/entregas/{id}/reingresar: la entrega y su nuevo estado. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ReingresoApi(Integer idEntrega, String estadoEntrega) {
}
