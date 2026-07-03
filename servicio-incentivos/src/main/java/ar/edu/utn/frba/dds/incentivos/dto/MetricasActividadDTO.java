package ar.edu.utn.frba.dds.incentivos.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MetricasActividadDTO {
    private String donanteNombre;
    private String categoriaActual;
    private String periodo;
    private int solicitudesDonacionHechas;
    private int beneficiariosAyudados;
    private int misionesCompletadas;
    private int insigniasObtenidas;
    private int impactoAcumulado;
    private Integer posicionRanking;
    private List<EvolucionMensualDTO> evolucionMensual;
}
