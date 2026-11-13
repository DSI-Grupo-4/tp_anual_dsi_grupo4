package ar.edu.utn.frba.dds.logistica.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import ar.edu.utn.frba.dds.logistica.domain.geo.Geo;
import ar.edu.utn.frba.dds.logistica.domain.geo.Punto;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Camion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Direccion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoCamion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoEntrega;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import ar.edu.utn.frba.dds.logistica.domain.rutas.GestorRutas;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Parada;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Ruta;
import ar.edu.utn.frba.dds.logistica.dto.SimulacionAvanceDTO;
import ar.edu.utn.frba.dds.logistica.repository.CamionRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SimuladorRutaServiceTest {

    private static final Punto DEPOSITO = new Punto(-34.6037, -58.3816);

    private final GestorRutas gestor = mock(GestorRutas.class);
    private final CamionRepository camiones = mock(CamionRepository.class);
    private final SimuladorRutaService simulador = new SimuladorRutaService(gestor, camiones, -34.6037, -58.3816, 0.25, 0.15, true);

    private Camion camion;

    @BeforeEach
    void camionNuevo() {
        camion = new Camion(1, "ABC123", 20, 3, 1000, EstadoCamion.DISPONIBLE);
    }

    private static Direccion direccion(Double lat, Double lng) {
        Direccion d = new Direccion("Calle", "1", null);
        d.setLatitud(lat);
        d.setLongitud(lng);
        return d;
    }

    private Parada parada(int numero, int entidad, Direccion direccion) {
        Entrega entrega = new Entrega(numero, numero + 100, entidad, direccion, LocalDate.now(), 10, 1, 1);
        entrega.asignarARuta(camion);
        return new Parada(numero, entidad, direccion, List.of(entrega));
    }

    /** Una ruta ya iniciada: sus entregas están en traslado. */
    private Ruta rutaIniciada(Parada... paradas) {
        Ruta ruta = new Ruta(1, camion, null, LocalDate.now(), List.of(paradas));
        ruta.iniciarRuta();
        when(gestor.buscarPorId(1)).thenReturn(ruta);
        return ruta;
    }

    @Test
    void elCamionAvanzaUnaParteDelCaminoHaciaLaProximaParadaYSeGuardaComoSimulado() {
        rutaIniciada(parada(1, 5, direccion(-34.6037, -58.3816)));
        camion.reportarUbicacion(-34.7037, -58.4816, java.time.LocalDateTime.now(), false);

        SimulacionAvanceDTO resultado = simulador.avanzar(1);

        assertThat(resultado.getUbicacion().getLatitud()).isCloseTo(-34.7037 + 0.25 * 0.1, within(1e-9));
        assertThat(resultado.getUbicacion().getLongitud()).isCloseTo(-58.4816 + 0.25 * 0.1, within(1e-9));
        assertThat(resultado.getUbicacion().isSimulada()).isTrue();
        assertThat(resultado.getNumeroParadaDestino()).isEqualTo(1);
        assertThat(resultado.isLlegoAlDestino()).isFalse();
        assertThat(resultado.isDestinoDeEjemplo()).isFalse();
        assertThat(resultado.getDistanciaRestanteKm()).isGreaterThan(0.0);
        verify(camiones).save(camion);
    }

    @Test
    void siYaEstaCercaLlegaAlDestinoQuedaExactamenteEnEl() {
        rutaIniciada(parada(1, 5, direccion(-34.6037, -58.3816)));
        camion.reportarUbicacion(-34.6039, -58.3818, java.time.LocalDateTime.now(), true);

        SimulacionAvanceDTO resultado = simulador.avanzar(1);

        assertThat(resultado.isLlegoAlDestino()).isTrue();
        assertThat(resultado.getDistanciaRestanteKm()).isZero();
        assertThat(resultado.getUbicacion().getLatitud()).isEqualTo(-34.6037);
        assertThat(resultado.getUbicacion().getLongitud()).isEqualTo(-58.3816);
    }

    @Test
    void sinUbicacionPreviaParteDelDeposito() {
        rutaIniciada(parada(1, 5, direccion(-34.5037, -58.2816)));

        SimulacionAvanceDTO resultado = simulador.avanzar(1);

        assertThat(resultado.getUbicacion().getLatitud()).isCloseTo(DEPOSITO.latitud() + 0.25 * 0.1, within(1e-9));
    }

    @Test
    void siLaParadaNoTieneCoordenadasUsaUnPuntoDeEjemploYLoAvisa() {
        rutaIniciada(parada(1, 7, direccion(null, null)));

        SimulacionAvanceDTO resultado = simulador.avanzar(1);

        Punto esperado = Geo.avanzar(DEPOSITO, Geo.puntoDeEjemplo(7, DEPOSITO), 0.25);
        assertThat(resultado.isDestinoDeEjemplo()).isTrue();
        assertThat(resultado.getUbicacion().getLatitud()).isCloseTo(esperado.latitud(), within(1e-9));
        assertThat(resultado.getUbicacion().getLongitud()).isCloseTo(esperado.longitud(), within(1e-9));
    }

    @Test
    void conLosPuntosDeEjemploDeshabilitadosUnaParadaSinCoordenadasSeRechaza() {
        SimuladorRutaService estricto = new SimuladorRutaService(gestor, camiones, -34.6037, -58.3816, 0.25, 0.15, false);
        rutaIniciada(parada(1, 7, direccion(null, null)));

        assertThatThrownBy(() -> estricto.avanzar(1))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("no tiene coordenadas");
        verify(camiones, never()).save(any());
    }

    @Test
    void vaHaciaLaPrimeraParadaQueTodaviaTieneEntregasEnTraslado() {
        Parada primera = parada(1, 5, direccion(-34.6037, -58.3816));
        Parada segunda = parada(2, 6, direccion(-34.5037, -58.2816));
        rutaIniciada(primera, segunda);
        primera.confirmarRecepcion(null); // la primera ya se entregó

        SimulacionAvanceDTO resultado = simulador.avanzar(1);

        assertThat(resultado.getNumeroParadaDestino()).isEqualTo(2);
    }

    @Test
    void unaRutaQueNoEstaEnCursoNoSeSimula() {
        Ruta planificada = new Ruta(1, camion, null, LocalDate.now(), List.of(parada(1, 5, direccion(-34.6, -58.4))));
        when(gestor.buscarPorId(1)).thenReturn(planificada);

        assertThatThrownBy(() -> simulador.avanzar(1))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("no está en curso");
        verify(camiones, never()).save(any());
    }

    @Test
    void sinParadasPendientesNoHayHaciaDondeAvanzar() {
        Parada unica = parada(1, 5, direccion(-34.6, -58.4));
        rutaIniciada(unica);
        unica.confirmarRecepcion(null);
        assertThat(unica.getEntregas().get(0).getEstadoEntrega()).isEqualTo(EstadoEntrega.ENTREGADA);

        assertThatThrownBy(() -> simulador.avanzar(1))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("no tiene paradas pendientes");
    }
}
