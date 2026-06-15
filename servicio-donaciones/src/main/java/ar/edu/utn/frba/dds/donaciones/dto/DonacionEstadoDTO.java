package ar.edu.utn.frba.dds.donaciones.dto;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoDonacion;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DonacionEstadoDTO {
    private EstadoDonacion nuevoEstado;
    private String motivo;
}
