package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.NecesidadRecurrente;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Periodicidad;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaJuridica;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoOrganizacion;
import org.junit.jupiter.api.Test;
import ar.edu.utn.frba.dds.donaciones.DatosPrueba;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.UnidadMedida;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DonacionTest {

    private ItemDonado itemDeEjemplo() {
        return DatosPrueba.item(1L, "Frazadas", DatosPrueba.subcategoria("frazadas"), 10, null);
    }

    @Test
    void alCrearseQuedaEnDepositoConHistorialInicial() {
        Donacion donacion = DatosPrueba.donacion(1L, itemDeEjemplo(), 10);

        assertThat(donacion.getEstadoActual()).isEqualTo(EstadoTrack.EN_DEPOSITO);
        assertThat(donacion.getHistorialEstados()).hasSize(1);
        assertThat(donacion.getHistorialEstados().get(0).getEstadoNuevo()).isEqualTo(EstadoTrack.EN_DEPOSITO);
    }

    @Test
    void laMaquinaDeEstadosTieneExactamenteLos7EstadosDefinidos() {
        // Regresión: PENDIENTE_CONFIRMACION no forma parte de la máquina de
        // estados oficial y se eliminó del enum.
        assertThat(EstadoTrack.values())
                .extracting(Enum::name)
                .containsExactlyInAnyOrder(
                        "EN_DEPOSITO", "ASIGNACION_REALIZADA", "LISTA_PARA_ENTREGAR",
                        "EN_TRASLADO", "ENTREGADA", "ENTREGA_FALLIDA", "VENCIDA");
    }

    @Test
    void permiteLaSecuenciaCompletaDeTransicionesValidas() {
        Donacion donacion = DatosPrueba.donacion(1L, itemDeEjemplo(), 10);

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
        Donacion donacion = DatosPrueba.donacion(1L, itemDeEjemplo(), 10);

        assertThatThrownBy(() -> donacion.cambiarEstado(EstadoTrack.EN_TRASLADO, null))
                .isInstanceOf(EstadoInvalidoException.class);
    }

    @Test
    void entregaFallidaRequiereJustificacion() {
        Donacion donacion = DatosPrueba.donacion(1L, itemDeEjemplo(), 10);
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
        Donacion donacion = DatosPrueba.donacion(1L, itemDeEjemplo(), 10);
        donacion.cambiarEstado(EstadoTrack.ASIGNACION_REALIZADA, null);
        donacion.cambiarEstado(EstadoTrack.LISTA_PARA_ENTREGAR, null);
        donacion.cambiarEstado(EstadoTrack.EN_TRASLADO, null);
        donacion.cambiarEstado(EstadoTrack.ENTREGA_FALLIDA, "Nadie respondio");

        donacion.cambiarEstado(EstadoTrack.EN_DEPOSITO, null);

        assertThat(donacion.getEstadoActual()).isEqualTo(EstadoTrack.EN_DEPOSITO);
    }

    @Test
    void estaAsignadaSoloCuandoHayNecesidadAsignada() {
        Donacion sinAsignar = DatosPrueba.donacion(1L, itemDeEjemplo(), 10);
        assertThat(sinAsignar.estaAsignada()).isFalse();

        EntidadBeneficiaria entidad = new EntidadBeneficiaria(1L,
                new PersonaJuridica("Comedor Sonrisas", TipoOrganizacion.ONG, null, null),
                "Comedor infantil");
        Necesidad necesidad = new NecesidadRecurrente(1L, "Fideos semanales",
                DatosPrueba.subcategoria("fideos secos"), UnidadMedida.UNIDAD, BigDecimal.valueOf(100), Periodicidad.SEMANAL);

        Donacion asignada = DatosPrueba.donacion(2L, itemDeEjemplo(), 10, necesidad, entidad);
        assertThat(asignada.estaAsignada()).isTrue();
    }
}
