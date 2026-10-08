package ar.edu.utn.frba.dds.donaciones.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

// Espejo (parcial) de EventoLogistico de servicio-logistica, tal como llega
// por GET /api/eventos.
@Getter
@Setter
public class EventoLogisticoDTO {
    private UUID idEvento;
    private String tipoEvento;
    private EntregaEventoDTO entregaAsociada;
}
