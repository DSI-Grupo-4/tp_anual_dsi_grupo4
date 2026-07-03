package ar.edu.utn.frba.dds.incentivos.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MisionDisponibleDTO {
    private String nombreMision;
    private String categoriaNombre;
    private String insigniaNombre;
    private int progresoActual;
    private int distanciaRestante;
    private boolean completada;
    private boolean activa;
}
