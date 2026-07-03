package ar.edu.utn.frba.dds.donaciones.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntidadCandidataDTO {
    private Long entidadBeneficiariaId;
    private String razonSocial;
    private String descripcion;
    private Integer puntaje;
}