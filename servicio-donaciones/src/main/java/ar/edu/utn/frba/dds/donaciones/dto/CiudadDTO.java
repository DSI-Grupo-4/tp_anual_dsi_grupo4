package ar.edu.utn.frba.dds.donaciones.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CiudadDTO {
    private String nombre;
    private ProvinciaDTO provincia;
}
