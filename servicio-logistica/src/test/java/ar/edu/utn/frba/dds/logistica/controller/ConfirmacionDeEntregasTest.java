package ar.edu.utn.frba.dds.logistica.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import ar.edu.utn.frba.dds.logistica.exception.GlobalExceptionHandler;
import ar.edu.utn.frba.dds.logistica.service.ControlDeFotos;
import ar.edu.utn.frba.dds.logistica.service.RecepcionService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Confirmación de entregas de punta a punta dentro de Logística (controllers + RecepcionService + dominio reales;
 * repositorio, eventos y control de fotos simulados). POST .../recepcion y POST .../confirmar tienen que respetar las
 * mismas reglas: ruta INICIADA, entrega EN_TRASLADO y que sea de la entidad de la cuenta.
 * Qué roles pueden llamar a cada URL lo decide SeguridadApiConfig: eso se prueba con Keycloak, no acá.
 */
class ConfirmacionDeEntregasTest {

    private static final String FOTO = "http://l/api/fotos/6730a1f2c3d4e5f607182930";

    private final GestorRutas gestorRutas = mock(GestorRutas.class);
    private final GestorEventos gestorEventos = mock(GestorEventos.class);
    private MockMvc mvc;
    private Camion camion;
    private Entrega entrega;
    private Parada parada;
    private JwtAuthenticationToken entidad7;
    private JwtAuthenticationToken entidad8;
    private JwtAuthenticationToken administradora;

    private static JwtAuthenticationToken cuenta(String rol, Object... entidadIds) {
        Jwt.Builder jwt = Jwt.withTokenValue("token").header("alg", "none").subject("cuenta-" + rol)
                .issuedAt(Instant.parse("2026-11-13T00:00:00Z"));
        if (entidadIds.length > 0) {
            jwt.claim("entidad_ids", List.of(entidadIds));
        }
        return new JwtAuthenticationToken(jwt.build(), List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
    }

    @BeforeEach
    void preparar() {
        RecepcionService recepcion = new RecepcionService(gestorRutas, gestorEventos);
        mvc = MockMvcBuilders.standaloneSetup(
                        new RecepcionController(recepcion, mock(ControlDeFotos.class)),
                        new ParadaController(gestorRutas, gestorEventos, mock(ControlDeFotos.class), recepcion))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        entidad7 = cuenta("BENEFICIARIA", 7);
        entidad8 = cuenta("BENEFICIARIA", 8);
        administradora = cuenta("ADMIN");

        camion = new Camion(1, "ABC123", 20, 3, 1000, EstadoCamion.DISPONIBLE);
        entrega = new Entrega(1, 101, 7, new Direccion("Calle", "1", null), LocalDate.now(), 10, 1, 1);
        entrega.asignarARuta(camion);
        parada = new Parada(1, 7, new Direccion("Calle", "1", null), List.of(entrega));
    }

    private Ruta ruta(boolean iniciada, Parada... otrasParadas) {
        List<Parada> paradas = new java.util.ArrayList<>(List.of(parada));
        paradas.addAll(List.of(otrasParadas));
        Ruta ruta = new Ruta(1, camion, null, LocalDate.now(), paradas);
        if (iniciada) {
            ruta.iniciarRuta();
        }
        when(gestorRutas.buscarPorId(1)).thenReturn(ruta);
        return ruta;
    }

    private void confirmar(String endpoint, JwtAuthenticationToken cuenta, int estadoEsperado) throws Exception {
        String cuerpo = endpoint.equals("recepcion")
                ? "{\"fotos\":[{\"url\":\"" + FOTO + "\"}]}"
                : "{\"url\":\"" + FOTO + "\"}";
        mvc.perform(post("/api/rutas/1/paradas/1/" + endpoint).principal(cuenta)
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().is(estadoEsperado));
    }

    private void assertSinEfectos(EstadoEntrega estadoEsperado) {
        assertThat(entrega.getEstadoEntrega()).isEqualTo(estadoEsperado);
        assertThat(entrega.getFotos()).isEmpty();
        assertThat(entrega.getFechaHoraEntrega()).isNull();
        verify(gestorRutas, never()).guardar(any());
        verifyNoInteractions(gestorEventos);
    }

    @ParameterizedTest
    @ValueSource(strings = {"recepcion", "confirmar"})
    void laEntidadDestinatariaConfirmaUnaEntregaEnTraslado(String endpoint) throws Exception {
        Ruta ruta = ruta(true);

        confirmar(endpoint, entidad7, 200);

        assertThat(entrega.getEstadoEntrega()).isEqualTo(EstadoEntrega.ENTREGADA);
        assertThat(entrega.getFotos()).hasSize(1);
        assertThat(entrega.getFechaHoraEntrega()).isNotNull();
        assertThat(ruta.getEstadoRuta()).isEqualTo(EstadoRuta.FINALIZADA);
        verify(gestorEventos, times(1)).crearEvento(TipoEvento.ENTREGA_CONFIRMADA, entrega);
        verify(gestorRutas, times(1)).guardar(ruta);
    }

    @ParameterizedTest
    @ValueSource(strings = {"recepcion", "confirmar"})
    void confirmarAntesDeIniciarLaRutaEsUn409SinEfectos(String endpoint) throws Exception {
        ruta(false);

        confirmar(endpoint, entidad7, 409);

        assertSinEfectos(EstadoEntrega.ASIGNADA_A_RUTA);
    }

    @ParameterizedTest
    @ValueSource(strings = {"recepcion", "confirmar"})
    void laAdministracionTampocoPuedeConfirmarAntesDeTiempo(String endpoint) throws Exception {
        ruta(false);

        confirmar(endpoint, administradora, 409);

        assertSinEfectos(EstadoEntrega.ASIGNADA_A_RUTA);
    }

    @ParameterizedTest
    @ValueSource(strings = {"recepcion", "confirmar"})
    void laAdministracionPuedeConfirmarUnaEntregaEnTraslado(String endpoint) throws Exception {
        ruta(true);

        confirmar(endpoint, administradora, 200);

        assertThat(entrega.getEstadoEntrega()).isEqualTo(EstadoEntrega.ENTREGADA);
        verify(gestorEventos, times(1)).crearEvento(TipoEvento.ENTREGA_CONFIRMADA, entrega);
    }

    @ParameterizedTest
    @ValueSource(strings = {"recepcion", "confirmar"})
    void confirmarDosVecesEsUn409YNoRepiteFotosNiEventos(String endpoint) throws Exception {
        // Otra parada pendiente: la ruta sigue INICIADA después de la primera confirmación.
        Entrega otra = new Entrega(2, 102, 9, new Direccion("Calle", "2", null), LocalDate.now(), 10, 1, 1);
        otra.asignarARuta(camion);
        Ruta ruta = ruta(true, new Parada(2, 9, new Direccion("Calle", "2", null), List.of(otra)));
        confirmar(endpoint, entidad7, 200);

        confirmar(endpoint, entidad7, 409);

        assertThat(entrega.getFotos()).hasSize(1);
        verify(gestorEventos, times(1)).crearEvento(TipoEvento.ENTREGA_CONFIRMADA, entrega);
        verify(gestorRutas, times(1)).guardar(ruta);
        assertThat(otra.getEstadoEntrega()).isEqualTo(EstadoEntrega.EN_TRASLADO);
    }

    @ParameterizedTest
    @ValueSource(strings = {"recepcion", "confirmar"})
    void laEntidadNoPuedeConfirmarLaEntregaDeOtraEntidad(String endpoint) throws Exception {
        Ruta ruta = ruta(true);

        confirmar(endpoint, entidad8, 403);

        assertSinEfectos(EstadoEntrega.EN_TRASLADO);
        assertThat(ruta.getEstadoRuta()).isEqualTo(EstadoRuta.INICIADA);
    }

    @ParameterizedTest
    @ValueSource(strings = {"recepcion", "confirmar"})
    void unaEntregaYaMarcadaComoNoRecibidaNoSePuedeConfirmar(String endpoint) throws Exception {
        Ruta ruta = ruta(true);
        entrega.marcarNoRecibida("Nadie atendió");

        confirmar(endpoint, entidad7, 409);

        assertThat(entrega.getEstadoEntrega()).isEqualTo(EstadoEntrega.NO_RECIBIDA);
        assertThat(entrega.getFotos()).isEmpty();
        verify(gestorRutas, never()).guardar(ruta);
        verifyNoInteractions(gestorEventos);
    }
}
