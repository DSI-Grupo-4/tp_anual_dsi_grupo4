package ar.edu.utn.frba.dds.donaciones.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RankingEntidadDTO {
    private Long entidadBeneficiariaId;
    private String razonSocial;
    private String descripcion;
    private Integer puntaje;
}