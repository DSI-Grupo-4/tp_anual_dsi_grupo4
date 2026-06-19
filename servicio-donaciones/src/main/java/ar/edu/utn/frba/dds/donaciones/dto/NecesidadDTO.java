package ar.edu.utn.frba.dds.donaciones.dto;

import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Periodicidad;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.TipoExtraordinario;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NecesidadDTO {
    private Long id;
    private String tipo;
    private String descripcion;
    private String subcategoria;
    private Integer cantidadRequerida;
    private Integer cantidadRecibida;
    private Boolean satisfecha;
    private Long entidadBeneficiariaId;
    private Periodicidad periodicidad;
    private TipoExtraordinario tipoExtraordinario;
}