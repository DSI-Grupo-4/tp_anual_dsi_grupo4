package ar.edu.utn.frba.dds.donaciones.dto;


import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoDonacion;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DonacionDTO {
    private Long id;
    private String descripcionItem;
    private Integer cantidadAsignada;
    private EstadoDonacion estadoActual;
    private Long entidadBeneficiariaId;
    private Long necesidadId;
}
