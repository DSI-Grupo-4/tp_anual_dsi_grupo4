package ar.edu.utn.frba.dds.donaciones.dto;

import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Periodicidad;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NecesidadRecurrenteDTO {
    private String descripcion;
    private String subcategoria;
    private Integer cantidadRequerida;
    private Long entidadBeneficiariaId;
    private Periodicidad periodicidad;
}