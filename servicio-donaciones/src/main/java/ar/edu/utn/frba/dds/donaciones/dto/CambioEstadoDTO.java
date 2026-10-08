package ar.edu.utn.frba.dds.donaciones.dto;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambioEstadoDTO {
    private EstadoTrack nuevoEstado;
    private String justificacion;
    // Opcional: cuando el cambio de estado viene de un evento externo
    // (ej. Logística vía EventosLogisticaScheduler), permite que el tipoEvento
    // publicado hacia Notificaciones sea el real (RUTA_INICIADA,
    // ENTREGA_CONFIRMADA, etc.) en vez del genérico CAMBIO_ESTADO_DONACION
    // que se usa para cambios manuales (ej. PUT /api/donaciones/{id}/estado).
    private String origenEvento;
}
