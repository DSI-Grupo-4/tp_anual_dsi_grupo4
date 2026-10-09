package ar.edu.utn.frba.dds.donaciones.domain.necesidades;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import org.junit.jupiter.api.Test;
import ar.edu.utn.frba.dds.donaciones.DatosPrueba;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.UnidadMedida;
import java.math.BigDecimal;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class NecesidadRecurrenteTest {

    @Test
    void unaVezSatisfechaDentroDelMismoPeriodoSigueSatisfecha() {
        NecesidadRecurrente necesidad = new NecesidadRecurrente(
                1L, "Fideos semanales", DatosPrueba.subcategoria("fideos secos"), UnidadMedida.UNIDAD, BigDecimal.valueOf(100), Periodicidad.SEMANAL);

        necesidad.recibir(BigDecimal.valueOf(100));

        assertThat(necesidad.satisfecha()).isTrue();
    }

    @Test
    void alVencerElPeriodoSeReiniciaLaCantidadRecibida() {
        NecesidadRecurrente necesidad = new NecesidadRecurrente(
                1L, "Fideos semanales", DatosPrueba.subcategoria("fideos secos"), UnidadMedida.UNIDAD, BigDecimal.valueOf(100), Periodicidad.SEMANAL);
        necesidad.recibir(BigDecimal.valueOf(100));
        assertThat(necesidad.satisfecha()).isTrue();

        // Simula que pasó una semana sin tocar el reloj del sistema.
        necesidad.setInicioPeriodoActual(LocalDate.now().minusWeeks(2));

        assertThat(necesidad.satisfecha()).isFalse();
        assertThat(necesidad.getCantidadRecibida()).isZero();
    }

    @Test
    void variosPeriodosSinActividadNoRompenElAvanceDelPeriodo() {
        NecesidadRecurrente necesidad = new NecesidadRecurrente(
                1L, "Fideos diarios", DatosPrueba.subcategoria("fideos secos"), UnidadMedida.UNIDAD, BigDecimal.valueOf(10), Periodicidad.DIARIA);
        necesidad.setInicioPeriodoActual(LocalDate.now().minusDays(30));

        necesidad.recibir(BigDecimal.valueOf(5));

        assertThat(necesidad.getCantidadRecibida()).isEqualTo(BigDecimal.valueOf(5));
        assertThat(necesidad.getInicioPeriodoActual()).isEqualTo(LocalDate.now());
    }
}
