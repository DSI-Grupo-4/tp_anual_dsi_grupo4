package ar.edu.utn.frba.dds.donaciones.dto;

import ar.edu.utn.frba.dds.donaciones.domain.personas.Genero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonaHumanaDTO {
    private String nombre;
    private String apellido;
    private Integer edad;
    private String documento;
    private Genero genero;
}