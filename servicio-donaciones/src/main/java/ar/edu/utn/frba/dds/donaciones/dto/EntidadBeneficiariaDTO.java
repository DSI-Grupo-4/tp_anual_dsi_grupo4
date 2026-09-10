package ar.edu.utn.frba.dds.donaciones.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EntidadBeneficiariaDTO {
    private Long id;
    @NotNull(message = "personaJuridica es obligatoria")
    @Valid
    private PersonaJuridicaDTO personaJuridica;
    @NotBlank(message = "descripcion es obligatoria")
    private String descripcion;
    private DireccionDTO direccion;
}
