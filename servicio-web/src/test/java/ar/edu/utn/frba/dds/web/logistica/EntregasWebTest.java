package ar.edu.utn.frba.dds.web.logistica;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.utn.frba.dds.web.SesionDePrueba;
import ar.edu.utn.frba.dds.web.client.donaciones.DonacionesClient;
import ar.edu.utn.frba.dds.web.client.donaciones.EntidadApi;
import ar.edu.utn.frba.dds.web.client.logistica.CamionApi;
import ar.edu.utn.frba.dds.web.client.logistica.EntregaAdminApi;
import ar.edu.utn.frba.dds.web.client.logistica.EntregasClient;
import ar.edu.utn.frba.dds.web.client.logistica.ReingresoApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.ChoferApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.CiudadApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.DireccionApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.EntregaDeRutaApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.ParadaApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutasClient;
import ar.edu.utn.frba.dds.web.error.BackendRechazoException;
import ar.edu.utn.frba.dds.web.error.ServicioBackend;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Entregas de la administración: listado, reingreso, y «no recibida» / incidente por parada. */
@SpringBootTest
@AutoConfigureMockMvc
class EntregasWebTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RutasClient rutas;

    @MockitoBean
    private EntregasClient entregas;

    @MockitoBean
    private DonacionesClient donaciones;

    private static EntregaDeRutaApi entregaDeRuta(int id, String estado) {
        return new EntregaDeRutaApi(id, id + 100, estado, null, null, List.of());
    }

    private static RutaApi ruta(int id, String estado, EntregaDeRutaApi... entregas) {
        ParadaApi parada = new ParadaApi(1, 1, 1, new DireccionApi("Calle", "10", new CiudadApi("Escobar")), List.of(entregas));
        return new RutaApi(id, LocalDate.of(2026, 11, 12), estado, new CamionApi(1, "AA123BB", 20, 3, 1500, "EN_RUTA"),
                new ChoferApi(1, "Juan Pérez"), List.of(parada));
    }

    @BeforeEach
    void datosBase() {
        when(donaciones.entidades()).thenReturn(List.of(
                new EntidadApi(1L, true, "Comedor", new EntidadApi.PersonaJuridica("Fundación Demo"))));
    }

    // ================= botones por parada =================

    @Test
    void unaRutaEnCursoConEntregasEnTrasladoOfreceRegistrarNoRecibidaEIncidente() throws Exception {
        when(rutas.ruta(2)).thenReturn(ruta(2, "INICIADA", entregaDeRuta(1, "EN_TRASLADO")));

        mvc.perform(get("/admin/seguimiento/2").with(SesionDePrueba.conRol("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Registrar no recibida"),
                        containsString("Registrar incidente"),
                        containsString("id=\"modal-nr-1\""),
                        containsString("/admin/seguimiento/2/paradas/1/no-recibida"),
                        containsString("/admin/seguimiento/2/paradas/1/incidente"))));
    }

    @Test
    void siYaNoQuedanEntregasEnTrasladoONoEstaEnCursoNoSeOfreceNada() throws Exception {
        when(rutas.ruta(2)).thenReturn(ruta(2, "INICIADA", entregaDeRuta(1, "ENTREGADA")));
        when(rutas.ruta(3)).thenReturn(ruta(3, "PLANIFICADA", entregaDeRuta(2, "ASIGNADA_A_RUTA")));
        when(rutas.ruta(4)).thenReturn(ruta(4, "FINALIZADA", entregaDeRuta(3, "ENTREGADA")));

        for (int id : new int[] {2, 3, 4}) {
            mvc.perform(get("/admin/seguimiento/" + id).with(SesionDePrueba.conRol("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(content().string(not(containsString("Registrar no recibida"))));
        }
    }

    @Test
    void registrarNoRecibidaLimpiaElTextoLlamaALogisticaYVuelveAlDetalle() throws Exception {
        mvc.perform(post("/admin/seguimiento/2/paradas/1/no-recibida").with(SesionDePrueba.conRol("ADMIN")).with(csrf()).param("motivo", "   Nadie atendió  "))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/seguimiento/2"))
                .andExpect(flash().attributeExists("toasts"));

        verify(rutas).registrarNoRecibida(2, 1, "Nadie atendió");
    }

    @Test
    void registrarUnIncidenteTambienLlamaALogistica() throws Exception {
        mvc.perform(post("/admin/seguimiento/2/paradas/1/incidente").with(SesionDePrueba.conRol("ADMIN")).with(csrf()).param("motivo", "Calle cortada por obras"))
                .andExpect(redirectedUrl("/admin/seguimiento/2"))
                .andExpect(flash().attributeExists("toasts"));

        verify(rutas).registrarIncidente(2, 1, "Calle cortada por obras");
    }

    @Test
    void sinMotivoOConUnoDemasiadoLargoSeAvisaYNoSeLlamaALogistica() throws Exception {
        mvc.perform(post("/admin/seguimiento/2/paradas/1/no-recibida").with(SesionDePrueba.conRol("ADMIN")).with(csrf()).param("motivo", "   "))
                .andExpect(redirectedUrl("/admin/seguimiento/2"))
                .andExpect(flash().attributeExists("toasts"));
        mvc.perform(post("/admin/seguimiento/2/paradas/1/incidente").with(SesionDePrueba.conRol("ADMIN")).with(csrf()))
                .andExpect(redirectedUrl("/admin/seguimiento/2"));
        mvc.perform(post("/admin/seguimiento/2/paradas/1/incidente").with(SesionDePrueba.conRol("ADMIN")).with(csrf()).param("motivo", "x".repeat(301)))
                .andExpect(redirectedUrl("/admin/seguimiento/2"))
                .andExpect(flash().attributeExists("toasts"));

        verify(rutas, never()).registrarNoRecibida(anyInt(), anyInt(), anyString());
        verify(rutas, never()).registrarIncidente(anyInt(), anyInt(), anyString());
    }

    @Test
    void siLogisticaRechazaElRegistroSeMuestraSuMensajeComoToast() throws Exception {
        doThrow(new BackendRechazoException(ServicioBackend.LOGISTICA, 409,
                "La parada 1 no tiene entregas en traslado", Map.of()))
                .when(rutas).registrarNoRecibida(2, 1, "Nadie atendió");

        mvc.perform(post("/admin/seguimiento/2/paradas/1/no-recibida").with(SesionDePrueba.conRol("ADMIN")).with(csrf()).param("motivo", "Nadie atendió"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/seguimiento/2"))
                .andExpect(flash().attributeExists("toasts"));
    }

    // ================= listado de entregas =================

    private List<EntregaAdminApi> variasEntregas() {
        return List.of(
                new EntregaAdminApi(3, 103, 1, "NO_RECIBIDA", LocalDate.of(2026, 11, 12), null, "Nadie atendió"),
                new EntregaAdminApi(2, 102, 1, "ENTREGADA", LocalDate.of(2026, 11, 12), LocalDateTime.of(2026, 11, 12, 17, 30), null),
                new EntregaAdminApi(1, 101, 7, "EN_TRASLADO", LocalDate.of(2026, 11, 12), null, null));
    }

    @Test
    void elListadoMuestraEstadosMotivosYSoloOfreceReingresarLasQueNoSeConcretaron() throws Exception {
        when(entregas.entregas()).thenReturn(variasEntregas());

        mvc.perform(get("/admin/seguimiento/entregas").with(SesionDePrueba.conRol("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Fundación Demo"),
                        containsString("Entidad 7"),
                        containsString("Nadie atendió"),
                        containsString("12/11/2026 14:30"),
                        containsString("id=\"modal-reingresar-3\""),
                        not(containsString("modal-reingresar-2")),
                        not(containsString("modal-reingresar-1")))));
    }

    @Test
    void elFiltroPorEstadoMuestraSoloEsasEntregas() throws Exception {
        when(entregas.entregas()).thenReturn(variasEntregas());

        mvc.perform(get("/admin/seguimiento/entregas").with(SesionDePrueba.conRol("ADMIN")).param("estado", "ENTREGADA"))
                .andExpect(content().string(allOf(containsString("#102"), not(containsString("#103")))));
        mvc.perform(get("/admin/seguimiento/entregas").with(SesionDePrueba.conRol("ADMIN")).param("estado", "cualquiera"))
                .andExpect(content().string(allOf(containsString("#102"), containsString("#103"))));
    }

    @Test
    void reingresarUnaEntregaVuelveAlListadoConUnToast() throws Exception {
        when(entregas.reingresar(3)).thenReturn(new ReingresoApi(3, "REPLANIFICABLE"));

        mvc.perform(post("/admin/seguimiento/entregas/3/reingresar").with(SesionDePrueba.conRol("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/seguimiento/entregas"))
                .andExpect(flash().attributeExists("toasts"));

        verify(entregas).reingresar(3);
    }

    @Test
    void siLaEntregaNoPuedeReingresarSeExplicaConUnToastDeError() throws Exception {
        when(entregas.reingresar(2)).thenThrow(new BackendRechazoException(ServicioBackend.LOGISTICA, 409,
                "La entrega 2 no puede reingresar al depósito estando en estado ENTREGADA", Map.of()));

        mvc.perform(post("/admin/seguimiento/entregas/2/reingresar").with(SesionDePrueba.conRol("ADMIN")).with(csrf()))
                .andExpect(redirectedUrl("/admin/seguimiento/entregas"))
                .andExpect(flash().attributeExists("toasts"));
    }

    @Test
    void elSeguimientoEnlazaAlListadoDeEntregas() throws Exception {
        when(rutas.rutas()).thenReturn(List.of());

        mvc.perform(get("/admin/seguimiento").with(SesionDePrueba.conRol("ADMIN")))
                .andExpect(content().string(containsString("href=\"/admin/seguimiento/entregas\"")));
    }
}
