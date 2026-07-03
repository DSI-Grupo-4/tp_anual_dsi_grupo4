package ar.edu.utn.frba.dds.incentivos.metricas;

import lombok.Getter;

import java.time.YearMonth;

@Getter
public class EvolucionMensual {

    private final YearMonth mes;
    private final int solicitudes;
    private final int impacto;

    public EvolucionMensual(YearMonth mes, int solicitudes, int impacto) {
        this.mes = mes;
        this.solicitudes = solicitudes;
        this.impacto = impacto;
    }
}
