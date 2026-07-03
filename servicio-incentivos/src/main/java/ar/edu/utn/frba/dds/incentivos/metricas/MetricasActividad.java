package ar.edu.utn.frba.dds.incentivos.metricas;

import lombok.Getter;

import java.util.List;

@Getter
public class MetricasActividad {

    private final Periodo periodo;
    private final int solicitudesDonacionHechas;
    private final int beneficiariosAyudados;
    private final int misionesCompletadas;
    private final int insigniasObtenidas;
    private final int impactoAcumulado;
    private final Integer posicionRanking;
    private final List<EvolucionMensual> evolucionMensual;

    public MetricasActividad(Periodo periodo, int solicitudesDonacionHechas, int beneficiariosAyudados,
                              int misionesCompletadas, int insigniasObtenidas, int impactoAcumulado,
                              Integer posicionRanking, List<EvolucionMensual> evolucionMensual) {
        this.periodo = periodo;
        this.solicitudesDonacionHechas = solicitudesDonacionHechas;
        this.beneficiariosAyudados = beneficiariosAyudados;
        this.misionesCompletadas = misionesCompletadas;
        this.insigniasObtenidas = insigniasObtenidas;
        this.impactoAcumulado = impactoAcumulado;
        this.posicionRanking = posicionRanking;
        this.evolucionMensual = evolucionMensual;
    }
}
