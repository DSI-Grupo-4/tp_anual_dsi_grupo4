package ar.edu.utn.frba.dds.logistica.domain.rutas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ResolucionDeParadaTest {

    private static Entrega entrega(Camion camion, int id, boolean enTraslado) {
        Entrega entrega = new Entrega(id, id + 100, 5, new Direccion("Calle", "1", null), LocalDate.now(), 10, 1, 1);
        entrega.asignarARuta(camion);
        if (enTraslado) {
            entrega.iniciarTraslado();
        }
        return entrega;
    }

    private static Camion camion() {
        return new Camion(1, "ABC123", 20, 3, 1000, EstadoCamion.EN_RUTA);
    }

    @Test
    void noRecibidaSoloAfectaALasEntregasQueSeguianEnTraslado() {
        Camion camion = camion();
        Entrega yaRecibida = entrega(camion, 1, true);
        yaRecibida.confirmarEntrega(null);
        Entrega enTraslado = entrega(camion, 2, true);
        Parada parada = new Parada(1, 5, new Direccion("Calle", "1", null), List.of(yaRecibida, enTraslado));

        List<Entrega> afectadas = parada.marcarNoRecibidasLasEnTraslado("Nadie atendió");

        assertThat(afectadas).containsExactly(enTraslado);
        assertThat(enTraslado.getEstadoEntrega()).isEqualTo(EstadoEntrega.NO_RECIBIDA);
        assertThat(enTraslado.getJustificacionFallo()).isEqualTo("Nadie atendió");
        assertThat(yaRecibida.getEstadoEntrega()).isEqualTo(EstadoEntrega.ENTREGADA); // no se pisó
    }

    @Test
    void elIncidenteSoloAfectaALasEntregasQueSeguianEnTraslado() {
        Camion camion = camion();
        Entrega yaNoRecibida = entrega(camion, 1, true);
        yaNoRecibida.marcarNoRecibida("Nadie atendió");
        Entrega enTraslado = entrega(camion, 2, true);
        Parada parada = new Parada(1, 5, new Direccion("Calle", "1", null), List.of(yaNoRecibida, enTraslado));

        List<Entrega> afectadas = parada.marcarFalladasLasEnTraslado("Calle cortada");

        assertThat(afectadas).containsExactly(enTraslado);
        assertThat(enTraslado.getEstadoEntrega()).isEqualTo(EstadoEntrega.FALLIDA);
        assertThat(yaNoRecibida.getEstadoEntrega()).isEqualTo(EstadoEntrega.NO_RECIBIDA);
    }

    @Test
    void sinEntregasEnTrasladoNoHayNadaQueRegistrar() {
        Camion camion = camion();
        Parada sinSalir = new Parada(1, 5, new Direccion("Calle", "1", null), List.of(entrega(camion, 1, false)));

        assertThatThrownBy(() -> sinSalir.marcarNoRecibidasLasEnTraslado("Nadie atendió"))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("no tiene entregas en traslado");
        assertThatThrownBy(() -> sinSalir.marcarFalladasLasEnTraslado("Calle cortada"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(sinSalir.getEntregas().get(0).getEstadoEntrega()).isEqualTo(EstadoEntrega.ASIGNADA_A_RUTA);
    }

    @Test
    void laJustificacionYElMotivoSonObligatoriosYNoSeTocaNada() {
        Camion camion = camion();
        Entrega enTraslado = entrega(camion, 1, true);
        Parada parada = new Parada(1, 5, new Direccion("Calle", "1", null), List.of(enTraslado));

        assertThatThrownBy(() -> parada.marcarNoRecibidasLasEnTraslado("  "))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("justificación");
        assertThatThrownBy(() -> parada.marcarNoRecibidasLasEnTraslado(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> parada.marcarFalladasLasEnTraslado(""))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("motivo");
        assertThat(enTraslado.getEstadoEntrega()).isEqualTo(EstadoEntrega.EN_TRASLADO);
    }
}
