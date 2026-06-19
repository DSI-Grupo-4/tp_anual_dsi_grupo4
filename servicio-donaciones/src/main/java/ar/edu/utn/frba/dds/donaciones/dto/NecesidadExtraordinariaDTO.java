package ar.edu.utn.frba.dds.donaciones.dto;

import ar.edu.utn.frba.dds.donaciones.domain.necesidades.TipoExtraordinario;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NecesidadExtraordinariaDTO {
    private String descripcion;
    private String subcategoria;
    private Integer cantidadRequerida;
    private Long entidadBeneficiariaId;
    private TipoExtraordinario tipoExtraordinario;
}