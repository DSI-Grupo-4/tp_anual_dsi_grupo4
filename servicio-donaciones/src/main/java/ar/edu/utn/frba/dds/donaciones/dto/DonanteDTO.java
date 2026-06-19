package ar.edu.utn.frba.dds.donaciones.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DonanteDTO {
    private Long id;
    private String tipo;
    private String nombre;
    private String apellido;
    private String razonSocial;
    private String documento;
}
