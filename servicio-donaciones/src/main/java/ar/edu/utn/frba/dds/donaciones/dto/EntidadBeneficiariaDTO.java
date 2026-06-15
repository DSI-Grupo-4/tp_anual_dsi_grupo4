package ar.edu.utn.frba.dds.donaciones.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntidadBeneficiariaDTO {
    private PersonaJuridicaDTO personaJuridica;
    private String descripcion;
}
