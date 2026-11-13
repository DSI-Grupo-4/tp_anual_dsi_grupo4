package ar.edu.utn.frba.dds.web.incentivos;

import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.EvolucionMes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class GraficoEvolucionTest {

    @Test
    void descartaMesesIlegiblesYEscalaContraElMaximo() {
        var filas = GraficoEvolucion.filas(List.of(
                new EvolucionMes("2026-10", 2, null),
                new EvolucionMes("mal", 9, null),
                new EvolucionMes("2026-11", 4, BigDecimal.ONE)));
        assertThat(filas).hasSize(2);
        assertThat(filas.get(0).mes()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(filas.get(0).porcentaje()).isEqualTo(50);
        assertThat(filas.get(1).porcentaje()).isEqualTo(100);
        assertThat(filas.get(0).impacto()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void sinActividadLasBarrasQuedanEnCero() {
        assertThat(GraficoEvolucion.filas(List.of(new EvolucionMes("2026-11", 0, null))).get(0).porcentaje()).isZero();
        assertThat(GraficoEvolucion.filas(null)).isEmpty();
    }
}
