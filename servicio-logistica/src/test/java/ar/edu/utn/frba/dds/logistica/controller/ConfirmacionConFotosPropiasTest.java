package ar.edu.utn.frba.dds.logistica.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.utn.frba.dds.logistica.domain.eventos.GestorEventos;
import ar.edu.utn.frba.dds.logistica.domain.rutas.GestorRutas;
import ar.edu.utn.frba.dds.logistica.exception.GlobalExceptionHandler;
import ar.edu.utn.frba.dds.logistica.service.ControlDeFotos;
import ar.edu.utn.frba.dds.logistica.service.RecepcionService;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

/**
 * La confirmación de una recepción solo avanza si las fotos son de quien confirma y se limita a las entidades del token.
 * Si el control las rechaza, no se toca la ruta ni las entregas. Los dos endpoints (/recepcion y /confirmar) pasan por
 * el mismo RecepcionService.
 */
class ConfirmacionConFotosPropiasTest {

    private static final String CUERPO_RECEPCION = "{\"fotos\":[{\"url\":\"http://l/api/fotos/6730a1f2c3d4e5f607182930\"}]}";
    private static final String CUERPO_CONFIRMAR = "{\"url\":\"http://l/api/fotos/6730a1f2c3d4e5f607182930\"}";

    private final RecepcionService recepcion = mock(RecepcionService.class);
    private final GestorRutas gestorRutas = mock(GestorRutas.class);
    private final ControlDeFotos control = mock(ControlDeFotos.class);
    private MockMvc mvc;
    private JwtAuthenticationToken beneficiaria;
    private JwtAuthenticationToken administradora;

    private static Jwt.Builder jwt() {
        return Jwt.withTokenValue("token").header("alg", "none").subject("cuenta-1")
                .issuedAt(Instant.parse("2026-11-13T00:00:00Z"));
    }

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(
                        new RecepcionController(recepcion, control),
                        new ParadaController(gestorRutas, mock(GestorEventos.class), control, recepcion))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        beneficiaria = new JwtAuthenticationToken(jwt().claim("entidad_ids", List.of(7)).build(),
                List.of(new SimpleGrantedAuthority("ROLE_BENEFICIARIA")));
        administradora = new JwtAuthenticationToken(jwt().build(), List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    void conFotosPropiasLaRecepcionSeConfirmaLimitadaALasEntidadesDelToken() throws Exception {
        mvc.perform(post("/api/rutas/2/paradas/3/recepcion").principal(beneficiaria)
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO_RECEPCION))
                .andExpect(status().isOk());

        verify(control).exigirPropias(eq(beneficiaria), eq(List.of("http://l/api/fotos/6730a1f2c3d4e5f607182930")));
        verify(recepcion).confirmar(eq(2), eq(3), any(), eq(Set.of(7L)));
    }

    @Test
    void elConfirmarSimpleUsaElMismoServicioYLaMismaLimitacionDeEntidad() throws Exception {
        mvc.perform(post("/api/rutas/2/paradas/3/confirmar").principal(beneficiaria)
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO_CONFIRMAR))
                .andExpect(status().isOk());

        verify(recepcion).confirmar(eq(2), eq(3), any(), eq(Set.of(7L)));
        verify(gestorRutas, never()).guardar(any());
    }

    @Test
    void laAdministracionNoQuedaLimitadaPorEntidadPeroPasaPorElMismoServicio() throws Exception {
        mvc.perform(post("/api/rutas/2/paradas/3/confirmar").principal(administradora)
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO_CONFIRMAR))
                .andExpect(status().isOk());
        mvc.perform(post("/api/rutas/2/paradas/3/recepcion").principal(administradora)
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO_RECEPCION))
                .andExpect(status().isOk());

        verify(recepcion, org.mockito.Mockito.times(2)).confirmar(eq(2), eq(3), any(), isNull());
    }

    @Test
    void conFotosDeOtraCuentaLaRecepcionEsUn403YNoConfirmaNada() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN)).when(control).exigirPropias(any(), any());

        mvc.perform(post("/api/rutas/2/paradas/3/recepcion").principal(beneficiaria)
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO_RECEPCION))
                .andExpect(status().isForbidden());

        verify(recepcion, never()).confirmar(any(), any(), any(), any());
    }

    @Test
    void elConfirmarSimpleTambienExigeFotosPropiasYNoTocaLaRuta() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN)).when(control).exigirPropias(any(), any());

        mvc.perform(post("/api/rutas/2/paradas/3/confirmar").principal(beneficiaria)
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO_CONFIRMAR))
                .andExpect(status().isForbidden());

        verify(recepcion, never()).confirmar(any(), any(), any(), any());
        verify(gestorRutas, never()).buscarPorId(any());
    }
}
