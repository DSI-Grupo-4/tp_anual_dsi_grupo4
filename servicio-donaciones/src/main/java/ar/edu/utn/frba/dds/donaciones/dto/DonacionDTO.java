package ar.edu.utn.frba.dds.donaciones.dto;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DonacionDTO {
    private Long id;
    private String descripcionItem;
    private Integer cantidadAsignada;
    private EstadoTrack estadoActual;
    private Long entidadBeneficiariaId;
    private Long necesidadId;
}
