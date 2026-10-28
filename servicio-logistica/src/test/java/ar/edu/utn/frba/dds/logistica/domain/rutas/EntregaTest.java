package ar.edu.utn.frba.dds.logistica.domain.rutas;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntregaTest {

    private Direccion direccion() {
        return new Direccion("Falsa", "123", new Ciudad("CABA", new Provincia("Buenos Aires")));
    }

    private Entrega entregaNoRecibida() {
        Entrega entrega = new Entrega(1, 1, 1, direccion(), LocalDate.now(), 10, 1, 1);
        entrega.marcarNoRecibida("Tocamos timbre pero nadie respondió");
        return entrega;
    }

    @Test
    void unaEntregaNoRecibidaPuedeReingresarAlDepositoYQuedaReplanificable() {
        Entrega entrega = entregaNoRecibida();

        entrega.reingresarADeposito();

        assertThat(entrega.getEstadoEntrega()).isEqualTo(EstadoEntrega.REPLANIFICABLE);
    }

    @Test
    void unaEntregaFallidaPuedeReingresarAlDepositoYQuedaReplanificable() {
        Entrega entrega = new Entrega(1, 1, 1, direccion(), LocalDate.now(), 10, 1, 1);
        entrega.marcarFallida("Los bienes vencieron en el camino");

        entrega.reingresarADeposito();

        assertThat(entrega.getEstadoEntrega()).isEqualTo(EstadoEntrega.REPLANIFICABLE);
        assertThat(entrega.getJustificacionFallo()).isEqualTo("Los bienes vencieron en el camino");
    }

    @Test
    void marcarFallidaExigeUnMotivo() {
        Entrega entrega = new Entrega(1, 1, 1, direccion(), LocalDate.now(), 10, 1, 1);

        assertThatThrownBy(() -> entrega.marcarFallida(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void unaEntregaQueNoEstaNoRecibidaNiFallidaNoPuedeReingresar() {
        Entrega entrega = new Entrega(1, 1, 1, direccion(), LocalDate.now(), 10, 1, 1);

        assertThatThrownBy(entrega::reingresarADeposito)
                .isInstanceOf(IllegalStateException.class);
    }
}
