package ar.edu.utn.frba.dds.incentivos.metricas;

import lombok.Getter;
import java.math.BigDecimal;

import java.time.YearMonth;

@Getter
public class EvolucionMensual {

    private final YearMonth mes;
    private final int solicitudes;
    private final BigDecimal impacto;

    public EvolucionMensual(YearMonth mes, int solicitudes, BigDecimal impacto) {
        this.mes = mes;
        this.solicitudes = solicitudes;
        this.impacto = impacto;
    }
}
