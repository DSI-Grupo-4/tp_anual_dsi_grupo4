package ar.edu.utn.frba.dds.logistica.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.logistica.domain.eventos.GestorEventos;
import ar.edu.utn.frba.dds.logistica.domain.eventos.TipoEvento;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Camion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Direccion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoCamion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoEntrega;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoRuta;
import ar.edu.utn.frba.dds.logistica.domain.rutas.FotoEntrega;
import ar.edu.utn.frba.dds.logistica.domain.rutas.GestorRutas;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Parada;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Ruta;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RecepcionServiceTest {

    private final GestorRutas gestorRutas = mock(GestorRutas.class);
    private final GestorEventos gestorEventos = mock(GestorEventos.class);
    private final RecepcionService servicio = new RecepcionService(gestorRutas, gestorEventos);

    private Camion camion;

    @BeforeEach
    void camionNuevo() {
        camion = new Camion(1, "ABC123", 20, 3, 1000, EstadoCamion.DISPONIBLE);
    }

    private static FotoEntrega foto(String url) {
        return new FotoEntrega(url, null);
    }

    private Parada parada(int numero, int entidad, int... idsEntrega) {
        List<Entrega> entregas = new ArrayList<>();
        for (int id : idsEntrega) {
            Entrega entrega = new Entrega(id, id + 100, entidad, new Direccion("Calle", "1", null), LocalDate.now(), 10, 1, 1);
            entrega.asignarARuta(camion);
            entregas.add(entrega);
        }
        return new Parada(numero, entidad, new Direccion("Calle", "1", null), entregas);
    }

    private Ruta rutaIniciada(Parada... paradas) {
        Ruta ruta = new Ruta(1, camion, null, LocalDate.now(), List.of(paradas));
        ruta.iniciarRuta();
        when(gestorRutas.buscarPorId(1)).thenReturn(ruta);
        return ruta;
    }

    @Test
    void confirmaLasEntregasConTodasLasFotosGuardaYGeneraUnEventoPorEntrega() {
        Parada unica = parada(1, 5, 1, 2);
        Ruta ruta = rutaIniciada(unica);

        servicio.confirmar(1, unica.getIdParada(), List.of(foto("https://f.org/1.jpg"), foto("https://f.org/2.jpg")));

        for (Entrega entrega : unica.getEntregas()) {
            assertThat(entrega.getEstadoEntrega()).isEqualTo(EstadoEntrega.ENTREGADA);
            assertThat(entrega.getFotos()).extracting(FotoEntrega::getUrl).containsExactly("https://f.org/1.jpg", "https://f.org/2.jpg");
            assertThat(entrega.getFotos().get(0).getFecha()).isEqualTo(LocalDate.now());
            assertThat(entrega.getFechaHoraEntrega()).isNotNull();
            verify(gestorEventos).crearEvento(TipoEvento.ENTREGA_CONFIRMADA, entrega);
        }
        verify(gestorRutas).guardar(ruta);
    }

    @Test
    void siEsLaUltimaParadaPendienteFinalizaLaRutaYLiberaElCamion() {
        Parada unica = parada(1, 5, 1);
        Ruta ruta = rutaIniciada(unica);

        servicio.confirmar(1, unica.getIdParada(), List.of(foto("https://f.org/1.jpg")));

        assertThat(ruta.getEstadoRuta()).isEqualTo(EstadoRuta.FINALIZADA);
        assertThat(camion.getEstadoCamion()).isEqualTo(EstadoCamion.DISPONIBLE);
    }

    @Test
    void siQuedanOtrasParadasPendientesLaRutaSigueEnCurso() {
        Parada primera = parada(1, 5, 1);
        Parada segunda = parada(2, 6, 2);
        Ruta ruta = rutaIniciada(primera, segunda);

        servicio.confirmar(1, primera.getIdParada(), List.of(foto("https://f.org/1.jpg")));

        assertThat(ruta.getEstadoRuta()).isEqualTo(EstadoRuta.INICIADA);
        assertThat(segunda.getEntregas().get(0).getEstadoEntrega()).isEqualTo(EstadoEntrega.EN_TRASLADO);
    }

    @Test
    void lasEntregasQueYaTeniaResultadoNoSeTocanNiGeneranEvento() {
        Parada unica = parada(1, 5, 1, 2);
        rutaIniciada(unica);
        Entrega yaNoRecibida = unica.getEntregas().get(0);
        yaNoRecibida.marcarNoRecibida("Nadie atendió");

        servicio.confirmar(1, unica.getIdParada(), List.of(foto("https://f.org/1.jpg")));

        assertThat(yaNoRecibida.getEstadoEntrega()).isEqualTo(EstadoEntrega.NO_RECIBIDA);
        assertThat(yaNoRecibida.getFotos()).isEmpty();
        verify(gestorEventos, never()).crearEvento(TipoEvento.ENTREGA_CONFIRMADA, yaNoRecibida);
    }

    @Test
    void noSePuedeConfirmarDosVeces() {
        Parada primera = parada(1, 5, 1);
        Parada segunda = parada(2, 6, 2);
        rutaIniciada(primera, segunda);
        servicio.confirmar(1, primera.getIdParada(), List.of(foto("https://f.org/1.jpg")));

        assertThatThrownBy(() -> servicio.confirmar(1, primera.getIdParada(), List.of(foto("https://f.org/2.jpg"))))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("no tiene entregas en traslado");
        assertThat(primera.getEntregas().get(0).getFotos()).hasSize(1);
    }

    @Test
    void unaRutaQueNoEstaEnCursoNoPermiteConfirmar() {
        Parada unica = parada(1, 5, 1);
        Ruta planificada = new Ruta(1, camion, null, LocalDate.now(), List.of(unica));
        when(gestorRutas.buscarPorId(1)).thenReturn(planificada);

        assertThatThrownBy(() -> servicio.confirmar(1, unica.getIdParada(), List.of(foto("https://f.org/1.jpg"))))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("no está en curso");
        assertThat(unica.getEntregas().get(0).getEstadoEntrega()).isEqualTo(EstadoEntrega.ASIGNADA_A_RUTA);
        verify(gestorRutas, never()).guardar(planificada);
    }

    @Test
    void unaParadaInexistenteEsUn404() {
        rutaIniciada(parada(1, 5, 1));

        assertThatThrownBy(() -> servicio.confirmar(1, 99, List.of(foto("https://f.org/1.jpg"))))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void lasFotosInvalidasSeRechazanAntesDeTocarNada() {
        assertThatThrownBy(() -> servicio.confirmar(1, 1, List.of()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("al menos una foto");
        assertThatThrownBy(() -> servicio.confirmar(1, 1, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> servicio.confirmar(1, 1, java.util.Collections.nCopies(6, foto("https://f.org/1.jpg"))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("hasta 5");
        assertThatThrownBy(() -> servicio.confirmar(1, 1, List.of(foto("javascript:alert(1)"))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("http");
        assertThatThrownBy(() -> servicio.confirmar(1, 1, List.of(foto("/api/fotos/abc"))))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(gestorRutas, gestorEventos);
    }
}
