package ar.edu.utn.frba.dds.donaciones.dto;

import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoContacto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MedioContactoDTO {
    @NotNull private TipoContacto tipo;
    @NotBlank private String valor;
    @NotNull private Boolean esPreferido;
}
