package ar.edu.utn.frba.dds.incentivos.metricas;

import java.time.LocalDate;

public enum Periodo {
    MENSUAL(1),
    TRIMESTRAL(3),
    SEMESTRAL(6),
    ANUAL(12),
    HISTORICO(-1);

    private final int meses;

    Periodo(int meses) {
        this.meses = meses;
    }

    public LocalDate fechaDesde(LocalDate referencia) {
        return meses < 0 ? LocalDate.MIN : referencia.minusMonths(meses);
    }
}
