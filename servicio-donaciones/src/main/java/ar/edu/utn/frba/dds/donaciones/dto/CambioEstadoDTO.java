package ar.edu.utn.frba.dds.donaciones.dto;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambioEstadoDTO {
    private EstadoTrack nuevoEstado;
    private String justificacion;
}
