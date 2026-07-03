package ar.edu.utn.frba.dds.incentivos.metricas;

import lombok.Getter;

import java.util.List;

@Getter
public class MetricasSistema {

    private final int donantesActivos;
    private final int solicitudesDonacionTotales;
    private final int misionesCompletadasTotales;
    private final int insigniasOtorgadasTotales;
    private final List<EvolucionMensual> evolucionMensual;

    public MetricasSistema(int donantesActivos, int solicitudesDonacionTotales,
                            int misionesCompletadasTotales, int insigniasOtorgadasTotales,
                            List<EvolucionMensual> evolucionMensual) {
        this.donantesActivos = donantesActivos;
        this.solicitudesDonacionTotales = solicitudesDonacionTotales;
        this.misionesCompletadasTotales = misionesCompletadasTotales;
        this.insigniasOtorgadasTotales = insigniasOtorgadasTotales;
        this.evolucionMensual = evolucionMensual;
    }
}
