package ar.edu.utn.frba.dds.web.client.donaciones;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

/** Un cambio de estado de una donación (TimeStampDTO de GET /api/donaciones/{id}/historial). La fecha llega en UTC. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record HistorialEstadoApi(String estado, LocalDateTime fecha, String justificacion) {
}
