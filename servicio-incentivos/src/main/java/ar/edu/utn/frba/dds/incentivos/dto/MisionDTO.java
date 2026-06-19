package ar.edu.utn.frba.dds.incentivos.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MisionDTO {
    private String nombre;
    private int cantidadCompletada;
    private boolean estaCompleta;
    private float progreso;
}
