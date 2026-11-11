package ar.edu.utn.frba.dds.web.vista;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class FormatoFechasTest {

    private final FormatoFechas fechas = new FormatoFechas("America/Argentina/Buenos_Aires");

    @Test
    void convierteLaHoraUtcDeLasApisAHoraArgentina() {
        assertThat(fechas.fechaHora(LocalDateTime.of(2026, 11, 3, 17, 30))).isEqualTo("03/11/2026 14:30");
    }

    @Test
    void cerrarElDiaEnUtcPuedeSerElDiaAnteriorEnArgentina() {
        assertThat(fechas.fecha(LocalDateTime.of(2026, 11, 4, 1, 0))).isEqualTo("03/11/2026");
    }

    @Test
    void lasFechasSinHoraNoSeCorrenDeDia() {
        assertThat(fechas.fecha(LocalDate.of(2026, 11, 4))).isEqualTo("04/11/2026");
        assertThat(fechas.mes(LocalDate.of(2026, 11, 4))).isEqualTo("noviembre de 2026");
    }

    @Test
    void sinDatoMuestraUnGuion() {
        assertThat(fechas.fechaHora((LocalDateTime) null)).isEqualTo("—");
        assertThat(fechas.fecha((LocalDate) null)).isEqualTo("—");
    }
}
