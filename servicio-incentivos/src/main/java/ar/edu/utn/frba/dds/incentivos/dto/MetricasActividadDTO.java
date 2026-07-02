package ar.edu.utn.frba.dds.incentivos.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MetricasActividadDTO {
    private String periodo;
    private int solicitudesDonacionHechas;
    private int beneficiariosAyudados;
    private int misionesCompletadas;
    private int insigniasObtenidas;
}
