package ar.edu.utn.frba.dds.logistica.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import ar.edu.utn.frba.dds.logistica.domain.eventos.GestorEventos;
import ar.edu.utn.frba.dds.logistica.domain.eventos.TipoEvento;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Camion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Direccion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoCamion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoEntrega;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoRuta;
import ar.edu.utn.frba.dds.logistica.domain.rutas.GestorRutas;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Parada;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Ruta;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** «No recibida» e incidente: solo con la ruta en curso y solo sobre las entregas que seguían en traslado. */
class ParadaControllerResolucionTest {

    private final GestorRutas gestorRutas = mock(GestorRutas.class);
    private final GestorEventos gestorEventos = mock(GestorEventos.class);
    private final ParadaController controller = new ParadaController(gestorRutas, gestorEventos);

    private Camion camion;

    @BeforeEach
    void camionNuevo() {
        camion = new Camion(1, "ABC123", 20, 3, 1000, EstadoCamion.DISPONIBLE);
    }

    private Parada parada(int numero, int... ids) {
        List<Entrega> entregas = new java.util.ArrayList<>();
        for (int id : ids) {
            Entrega e = new Entrega(id, id + 100, 5, new Direccion("Calle", "1", null), LocalDate.now(), 10, 1, 1);
            e.asignarARuta(camion);
            entregas.add(e);
        }
        return new Parada(numero, 5, new Direccion("Calle", "1", null), entregas);
    }

    private Ruta rutaIniciada(Parada... paradas) {
        Ruta ruta = new Ruta(1, camion, null, LocalDate.now(), List.of(paradas));
        ruta.iniciarRuta();
        when(gestorRutas.buscarPorId(1)).thenReturn(ruta);
        return ruta;
    }

    @Test
    void noRecibidaMarcaSoloLasEntregasEnTrasladoYGeneraSusEventos() {
        Parada primera = parada(1, 1, 2);
        Parada segunda = parada(2, 3);
        Ruta ruta = rutaIniciada(primera, segunda);
        primera.getEntregas().get(0).confirmarEntrega(null); // la entrega 1 ya se había recibido

        controller.marcarNoRecibida(1, 1, "Nadie atendió");

        assertThat(primera.getEntregas().get(0).getEstadoEntrega()).isEqualTo(EstadoEntrega.ENTREGADA);
        assertThat(primera.getEntregas().get(1).getEstadoEntrega()).isEqualTo(EstadoEntrega.NO_RECIBIDA);
        verify(gestorEventos).crearEvento(TipoEvento.ENTREGA_NO_RECIBIDA, primera.getEntregas().get(1));
        verify(gestorEventos, never()).crearEvento(TipoEvento.ENTREGA_NO_RECIBIDA, primera.getEntregas().get(0));
        assertThat(ruta.getEstadoRuta()).isEqualTo(EstadoRuta.INICIADA); // la parada 2 sigue pendiente
        verify(gestorRutas).guardar(ruta);
    }

    @Test
    void siEraLaUltimaPendienteLaRutaFinalizaYElCamionQuedaLibre() {
        Parada unica = parada(1, 1);
        Ruta ruta = rutaIniciada(unica);

        controller.marcarFallida(1, 1, "Calle cortada");

        assertThat(unica.getEntregas().get(0).getEstadoEntrega()).isEqualTo(EstadoEntrega.FALLIDA);
        assertThat(unica.getEntregas().get(0).getJustificacionFallo()).isEqualTo("Calle cortada");
        assertThat(ruta.getEstadoRuta()).isEqualTo(EstadoRuta.FINALIZADA);
        assertThat(camion.getEstadoCamion()).isEqualTo(EstadoCamion.DISPONIBLE);
        verify(gestorEventos).crearEvento(TipoEvento.ENTREGA_FALLIDA, unica.getEntregas().get(0));
    }

    @Test
    void unaRutaQueNoEstaEnCursoNoPermiteRegistrarResultados() {
        Parada unica = parada(1, 1);
        Ruta planificada = new Ruta(1, camion, null, LocalDate.now(), List.of(unica));
        when(gestorRutas.buscarPorId(1)).thenReturn(planificada);

        assertThatThrownBy(() -> controller.marcarNoRecibida(1, 1, "Nadie atendió"))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("no está en curso");
        assertThatThrownBy(() -> controller.marcarFallida(1, 1, "Calle cortada"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(unica.getEntregas().get(0).getEstadoEntrega()).isEqualTo(EstadoEntrega.ASIGNADA_A_RUTA);
        verify(gestorEventos, never()).crearEvento(any(), any());
        verify(gestorRutas, never()).guardar(any());
    }

    @Test
    void unaParadaSinEntregasEnTrasladoNoSePuedeVolverARegistrar() {
        Parada primera = parada(1, 1);
        Parada segunda = parada(2, 2);
        rutaIniciada(primera, segunda);
        controller.marcarNoRecibida(1, 1, "Nadie atendió");

        assertThatThrownBy(() -> controller.marcarNoRecibida(1, 1, "Otra vez"))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("no tiene entregas en traslado");
        assertThat(primera.getEntregas().get(0).getJustificacionFallo()).isEqualTo("Nadie atendió");
    }

    @Test
    void sinJustificacionOMotivoSeRechazaYNoCambiaNada() {
        Parada unica = parada(1, 1);
        rutaIniciada(unica);

        assertThatThrownBy(() -> controller.marcarNoRecibida(1, 1, " "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> controller.marcarFallida(1, 1, ""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(unica.getEntregas().get(0).getEstadoEntrega()).isEqualTo(EstadoEntrega.EN_TRASLADO);
    }
}
