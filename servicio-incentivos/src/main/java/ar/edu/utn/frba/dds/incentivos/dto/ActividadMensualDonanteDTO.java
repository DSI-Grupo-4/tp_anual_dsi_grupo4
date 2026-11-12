package ar.edu.utn.frba.dds.incentivos.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActividadMensualDonanteDTO {
    private Long donanteId;
    private int cantidad;
    private int puesto;
    /** Nombre del donante en Incentivos, para mostrar el ranking sin otra consulta. */
    private String donanteNombre;
}
