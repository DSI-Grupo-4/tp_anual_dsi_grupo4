package ar.edu.utn.frba.dds.logistica.domain.rutas;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntregaTest {

    private Entrega entregaNoRecibida() {
        Direccion direccion = new Direccion("Falsa", "123", new Ciudad("CABA", new Provincia("Buenos Aires")));
        Entrega entrega = new Entrega(1, 1, 1, direccion, LocalDate.now(), 10, 1, 1);
        entrega.marcarNoRecibida("Tocamos timbre pero nadie respondió");
        return entrega;
    }

    @Test
    void unaEntregaNoRecibidaPuedeReingresarAlDepositoYQuedaPendiente() {
        Entrega entrega = entregaNoRecibida();

        entrega.reingresarADeposito();

        assertThat(entrega.getEstadoEntrega()).isEqualTo(EstadoEntrega.PENDIENTE);
    }

    @Test
    void unaEntregaQueNoEstaNoRecibidaNoPuedeReingresar() {
        Direccion direccion = new Direccion("Falsa", "123", new Ciudad("CABA", new Provincia("Buenos Aires")));
        Entrega entrega = new Entrega(1, 1, 1, direccion, LocalDate.now(), 10, 1, 1);

        assertThatThrownBy(entrega::reingresarADeposito)
                .isInstanceOf(IllegalStateException.class);
    }
}
