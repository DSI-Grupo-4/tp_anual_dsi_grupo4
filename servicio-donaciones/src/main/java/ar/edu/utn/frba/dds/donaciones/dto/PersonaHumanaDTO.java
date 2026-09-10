package ar.edu.utn.frba.dds.donaciones.dto;

import ar.edu.utn.frba.dds.donaciones.domain.personas.Genero;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonaHumanaDTO {
    @NotBlank(message = "nombre es obligatorio")
    private String nombre;
    @NotBlank(message = "apellido es obligatorio")
    private String apellido;
    private Integer edad;
    @NotBlank(message = "documento es obligatorio")
    private String documento;
    private Genero genero;
}
