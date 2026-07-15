package ar.edu.utn.frba.dds.incentivos.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MetricasSistemaDTO {
    private int donantesActivos;
    private int solicitudesDonacionTotales;
    private int misionesCompletadasTotales;
    private int insigniasOtorgadasTotales;
    private List<EvolucionMensualDTO> evolucionMensual;
}
