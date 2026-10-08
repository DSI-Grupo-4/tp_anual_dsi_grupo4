package ar.edu.utn.frba.dds.donaciones.domain.necesidades;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class NecesidadRecurrenteTest {

    @Test
    void unaVezSatisfechaDentroDelMismoPeriodoSigueSatisfecha() {
        NecesidadRecurrente necesidad = new NecesidadRecurrente(
                1L, "Fideos semanales", new Subcategoria("fideos secos"), 100, Periodicidad.SEMANAL);

        necesidad.recibir(100);

        assertThat(necesidad.satisfecha()).isTrue();
    }

    @Test
    void alVencerElPeriodoSeReiniciaLaCantidadRecibida() {
        NecesidadRecurrente necesidad = new NecesidadRecurrente(
                1L, "Fideos semanales", new Subcategoria("fideos secos"), 100, Periodicidad.SEMANAL);
        necesidad.recibir(100);
        assertThat(necesidad.satisfecha()).isTrue();

        // Simula que pasó una semana sin tocar el reloj del sistema.
        necesidad.setInicioPeriodoActual(LocalDate.now().minusWeeks(2));

        assertThat(necesidad.satisfecha()).isFalse();
        assertThat(necesidad.getCantidadRecibida()).isZero();
    }

    @Test
    void variosPeriodosSinActividadNoRompenElAvanceDelPeriodo() {
        NecesidadRecurrente necesidad = new NecesidadRecurrente(
                1L, "Fideos diarios", new Subcategoria("fideos secos"), 10, Periodicidad.DIARIA);
        necesidad.setInicioPeriodoActual(LocalDate.now().minusDays(30));

        necesidad.recibir(5);

        assertThat(necesidad.getCantidadRecibida()).isEqualTo(5);
        assertThat(necesidad.getInicioPeriodoActual()).isEqualTo(LocalDate.now());
    }
}
