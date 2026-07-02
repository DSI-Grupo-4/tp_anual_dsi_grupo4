package ar.edu.utn.frba.dds.incentivos.metricas;

import lombok.Getter;

@Getter
public class MetricasActividad {

    private final Periodo periodo;
    private final int solicitudesDonacionHechas;
    private final int beneficiariosAyudados;
    private final int misionesCompletadas;
    private final int insigniasObtenidas;

    public MetricasActividad(Periodo periodo, int solicitudesDonacionHechas, int beneficiariosAyudados,
                              int misionesCompletadas, int insigniasObtenidas) {
        this.periodo = periodo;
        this.solicitudesDonacionHechas = solicitudesDonacionHechas;
        this.beneficiariosAyudados = beneficiariosAyudados;
        this.misionesCompletadas = misionesCompletadas;
        this.insigniasObtenidas = insigniasObtenidas;
    }
}
