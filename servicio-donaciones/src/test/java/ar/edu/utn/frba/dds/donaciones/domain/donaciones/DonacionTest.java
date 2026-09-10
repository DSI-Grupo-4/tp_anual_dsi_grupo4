package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.NecesidadRecurrente;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Periodicidad;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaJuridica;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoOrganizacion;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DonacionTest {

    private ItemDonado itemDeEjemplo() {
        return new ItemDonado(1L, "Frazadas", new Subcategoria("frazadas"), 10, null);
    }

    @Test
    void alCrearseQuedaEnDepositoConHistorialInicial() {
        Donacion donacion = new Donacion(1L, itemDeEjemplo(), 10);

        assertThat(donacion.getEstadoActual()).isEqualTo(EstadoTrack.EN_DEPOSITO);
        assertThat(donacion.getHistorialEstados()).hasSize(1);
        assertThat(donacion.getHistorialEstados().get(0).getEstadoNuevo()).isEqualTo(EstadoTrack.EN_DEPOSITO);
    }

    @Test
    void laMaquinaDeEstadosTieneExactamenteLos7EstadosDeLaFigura2DelPdf() {
        // Regresión D-003: PENDIENTE_CONFIRMACION no está en la Figura 2
        // oficial de la consigna y se eliminó del enum.
        assertThat(EstadoTrack.values())
                .extracting(Enum::name)
                .containsExactlyInAnyOrder(
                        "EN_DEPOSITO", "ASIGNACION_REALIZADA", "LISTA_PARA_ENTREGAR",
                        "EN_TRASLADO", "ENTREGADA", "ENTREGA_FALLIDA", "VENCIDA");
    }

    @Test
    void permiteLaSecuenciaCompletaDeTransicionesValidas() {
        Donacion donacion = new Donacion(1L, itemDeEjemplo(), 10);

        assertThatCode(() -> {
            donacion.cambiarEstado(EstadoTrack.ASIGNACION_REALIZADA, null);
            donacion.cambiarEstado(EstadoTrack.LISTA_PARA_ENTREGAR, null);
            donacion.cambiarEstado(EstadoTrack.EN_TRASLADO, null);
            donacion.cambiarEstado(EstadoTrack.ENTREGADA, null);
        }).doesNotThrowAnyException();

        assertThat(donacion.getEstadoActual()).isEqualTo(EstadoTrack.ENTREGADA);
        assertThat(donacion.getHistorialEstados()).hasSize(5); // inicial + 4 cambios
    }

    @Test
    void rechazaUnaTransicionQueSalteaPasos() {
        Donacion donacion = new Donacion(1L, itemDeEjemplo(), 10);

        assertThatThrownBy(() -> donacion.cambiarEstado(EstadoTrack.EN_TRASLADO, null))
                .isInstanceOf(EstadoInvalidoException.class);
    }

    @Test
    void entregaFallidaRequiereJustificacion() {
        Donacion donacion = new Donacion(1L, itemDeEjemplo(), 10);
        donacion.cambiarEstado(EstadoTrack.ASIGNACION_REALIZADA, null);
        donacion.cambiarEstado(EstadoTrack.LISTA_PARA_ENTREGAR, null);
        donacion.cambiarEstado(EstadoTrack.EN_TRASLADO, null);

        assertThatThrownBy(() -> donacion.cambiarEstado(EstadoTrack.ENTREGA_FALLIDA, null))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatCode(() ->
                donacion.cambiarEstado(EstadoTrack.ENTREGA_FALLIDA, "Tocamos timbre pero nadie respondio"))
                .doesNotThrowAnyException();
    }

    @Test
    void entregaFallidaPuedeVolverADeposito() {
        Donacion donacion = new Donacion(1L, itemDeEjemplo(), 10);
        donacion.cambiarEstado(EstadoTrack.ASIGNACION_REALIZADA, null);
        donacion.cambiarEstado(EstadoTrack.LISTA_PARA_ENTREGAR, null);
        donacion.cambiarEstado(EstadoTrack.EN_TRASLADO, null);
        donacion.cambiarEstado(EstadoTrack.ENTREGA_FALLIDA, "Nadie respondio");

        donacion.cambiarEstado(EstadoTrack.EN_DEPOSITO, null);

        assertThat(donacion.getEstadoActual()).isEqualTo(EstadoTrack.EN_DEPOSITO);
    }

    @Test
    void estaAsignadaSoloCuandoHayNecesidadAsignada() {
        Donacion sinAsignar = new Donacion(1L, itemDeEjemplo(), 10);
        assertThat(sinAsignar.estaAsignada()).isFalse();

        EntidadBeneficiaria entidad = new EntidadBeneficiaria(1L,
                new PersonaJuridica("Comedor Sonrisas", TipoOrganizacion.ONG, null, null),
                "Comedor infantil");
        Necesidad necesidad = new NecesidadRecurrente(1L, "Fideos semanales",
                new Subcategoria("fideos secos"), 100, Periodicidad.SEMANAL);

        Donacion asignada = new Donacion(2L, itemDeEjemplo(), 10, necesidad, entidad);
        assertThat(asignada.estaAsignada()).isTrue();
    }
}
