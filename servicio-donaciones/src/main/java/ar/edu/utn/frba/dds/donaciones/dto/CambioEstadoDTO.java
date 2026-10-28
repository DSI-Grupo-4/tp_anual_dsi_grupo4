package ar.edu.utn.frba.dds.donaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(example = """
{
  "nuevoEstado": "LISTA_PARA_ENTREGAR",
  "justificacion": "Ruta planificada para la entrega"
}
""")
public class CambioEstadoDTO {
    @jakarta.validation.constraints.NotNull
    private EstadoTrack nuevoEstado;
    private String justificacion;
    // Opcional: cuando el cambio de estado viene de un evento externo
    // (ej. Logística vía EventosLogisticaScheduler), permite que el tipoEvento
    // publicado hacia Notificaciones sea el real (RUTA_INICIADA,
    // ENTREGA_CONFIRMADA, etc.) en vez del genérico CAMBIO_ESTADO_DONACION
    // que se usa para cambios manuales (ej. PUT /api/donaciones/{id}/estado).
    private String origenEvento;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String eventoId;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String seguimientoUrl;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private java.time.LocalDateTime fechaHoraEntrega;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String patenteCamion;
}
