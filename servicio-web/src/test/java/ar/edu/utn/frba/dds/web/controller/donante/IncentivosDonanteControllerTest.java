package ar.edu.utn.frba.dds.web.controller.donante;

import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.Mision;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class IncentivosDonanteControllerTest {

    @Test
    void laEscaleraMarcaLasCategoriasAlcanzadasYLaActual() {
        var escalera = IncentivosDonanteController.escalera("Sostenedor");
        assertThat(escalera).extracting(IncentivosDonanteController.Escalon::alcanzada).containsExactly(true, true, false);
        assertThat(escalera).extracting(IncentivosDonanteController.Escalon::actual).containsExactly(false, true, false);
        assertThat(IncentivosDonanteController.escalera(null)).noneMatch(IncentivosDonanteController.Escalon::alcanzada);
    }

    @Test
    void siguienteCategoria() {
        assertThat(IncentivosDonanteController.siguienteCategoria("Colaborador")).isEqualTo("Sostenedor");
        assertThat(IncentivosDonanteController.siguienteCategoria("Transformador")).isNull();
        assertThat(IncentivosDonanteController.siguienteCategoria(null)).isEqualTo("Colaborador");
    }

    @Test
    void porcentajeDeUnaMision() {
        assertThat(mision(BigDecimal.ONE, BigDecimal.ONE, false, BigDecimal.valueOf(2)).porcentaje()).isEqualTo(50);
        assertThat(mision(BigDecimal.ONE, BigDecimal.ONE, true, BigDecimal.valueOf(2)).porcentaje()).isEqualTo(100);
        // Sin meta (Incentivos anterior a este cambio): progreso + lo que falta.
        assertThat(mision(BigDecimal.ONE, BigDecimal.valueOf(3), false, null).porcentaje()).isEqualTo(25);
        assertThat(mision(BigDecimal.ZERO, BigDecimal.ZERO, false, BigDecimal.ZERO).porcentaje()).isZero();
    }

    private static Mision mision(BigDecimal progreso, BigDecimal falta, boolean completada, BigDecimal meta) {
        return new Mision("M", "Colaborador", "I", progreso, falta, completada, !completada, meta, null);
    }
}
