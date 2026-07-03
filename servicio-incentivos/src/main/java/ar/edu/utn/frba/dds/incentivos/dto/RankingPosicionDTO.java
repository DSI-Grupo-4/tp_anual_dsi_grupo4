package ar.edu.utn.frba.dds.incentivos.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RankingPosicionDTO {
    private int puesto;
    private Long donanteId;
    private int cantidadMisiones;
    private String mes;
}
