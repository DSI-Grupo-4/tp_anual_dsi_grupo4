package ar.edu.utn.frba.dds.logistica.domain.rutas;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConfirmacionConFotosTest {

    private static Entrega entregaEnTraslado(Camion camion, int id) {
        Entrega entrega = new Entrega(id, id + 100, 5, new Direccion("Calle", "1", null), LocalDate.now(), 10, 1, 1);
        entrega.asignarARuta(camion);
        entrega.iniciarTraslado();
        return entrega;
    }

    @Test
    void cadaEntregaGuardaSuPropiaCopiaDeCadaFotoYCompletaLaFechaSiFalta() {
        Camion camion = new Camion(1, "ABC123", 20, 3, 1000, EstadoCamion.EN_RUTA);
        Entrega a = entregaEnTraslado(camion, 1);
        Entrega b = entregaEnTraslado(camion, 2);
        List<FotoEntrega> fotos = List.of(new FotoEntrega("https://f.org/1.jpg", null),
                new FotoEntrega("https://f.org/2.jpg", LocalDate.of(2026, 11, 3)));

        a.confirmarEntregaConFotos(fotos);
        b.confirmarEntregaConFotos(fotos);

        assertThat(a.getFotos()).hasSize(2);
        assertThat(a.getFotos().get(0)).isNotSameAs(b.getFotos().get(0));
        assertThat(a.getFotos().get(0).getFecha()).isEqualTo(LocalDate.now());
        assertThat(a.getFotos().get(1).getFecha()).isEqualTo(LocalDate.of(2026, 11, 3));
        assertThat(a.getEstadoEntrega()).isEqualTo(EstadoEntrega.ENTREGADA);
        assertThat(a.getFechaHoraEntrega()).isNotNull();
    }

    @Test
    void laParadaSoloConfirmaLasEntregasEnTrasladoYDevuelveLasConfirmadas() {
        Camion camion = new Camion(1, "ABC123", 20, 3, 1000, EstadoCamion.EN_RUTA);
        Entrega enTraslado = entregaEnTraslado(camion, 1);
        Entrega fallida = entregaEnTraslado(camion, 2);
        fallida.marcarFallida("Calle cortada");
        Parada parada = new Parada(1, 5, new Direccion("Calle", "1", null), List.of(enTraslado, fallida));

        List<Entrega> confirmadas = parada.confirmarRecepcionConFotos(List.of(new FotoEntrega("https://f.org/1.jpg", null)));

        assertThat(confirmadas).containsExactly(enTraslado);
        assertThat(fallida.getEstadoEntrega()).isEqualTo(EstadoEntrega.FALLIDA);
        assertThat(fallida.getFotos()).isEmpty();
        assertThat(parada.entregasEnTraslado()).isEmpty();
    }
}
