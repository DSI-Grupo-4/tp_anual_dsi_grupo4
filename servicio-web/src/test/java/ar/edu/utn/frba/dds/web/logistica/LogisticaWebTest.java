package ar.edu.utn.frba.dds.web.logistica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.utn.frba.dds.web.SesionDePrueba;
import ar.edu.utn.frba.dds.web.client.donaciones.DonacionesClient;
import ar.edu.utn.frba.dds.web.client.donaciones.EntidadApi;
import ar.edu.utn.frba.dds.web.client.logistica.CamionApi;
import ar.edu.utn.frba.dds.web.client.logistica.CamionRequest;
import ar.edu.utn.frba.dds.web.client.logistica.CamionesClient;
import ar.edu.utn.frba.dds.web.client.logistica.EntregaApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.ChoferApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.CiudadApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.DireccionApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.EntregaDeRutaApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.ParadaApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutasClient;
import ar.edu.utn.frba.dds.web.client.logistica.SimulacionApi;
import ar.edu.utn.frba.dds.web.error.BackendNoDisponibleException;
import ar.edu.utn.frba.dds.web.error.BackendRechazoException;
import ar.edu.utn.frba.dds.web.error.RecursoNoEncontradoException;
import ar.edu.utn.frba.dds.web.error.ServicioBackend;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Camiones y seguimiento de rutas (administración y entidad), con Logística y Donaciones simulados. La entidad de las pruebas es la 1. */
@SpringBootTest
@AutoConfigureMockMvc
class LogisticaWebTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CamionesClient camiones;

    @MockitoBean
    private RutasClient rutas;

    @MockitoBean
    private DonacionesClient donaciones;

    private static CamionApi camion(int id, String patente, String estado) {
        return new CamionApi(id, patente, 20, 3, 1500, estado);
    }

    private static EntregaDeRutaApi entrega(int id, String estado, String fotoUrl) {
        List<EntregaApi.FotoApi> fotos = fotoUrl == null ? List.of() : List.of(new EntregaApi.FotoApi(fotoUrl, LocalDate.of(2026, 11, 12)));
        return new EntregaDeRutaApi(id, id + 100, estado,
                "ENTREGADA".equals(estado) ? LocalDateTime.of(2026, 11, 12, 17, 30) : null, null, fotos);
    }

    private static ParadaApi parada(int numero, int entidad, String calle, EntregaDeRutaApi... entregas) {
        return new ParadaApi(numero, numero, entidad, new DireccionApi(calle, "10", new CiudadApi("Escobar")), List.of(entregas));
    }

    private static RutaApi ruta(int id, String estado, ParadaApi... paradas) {
        return new RutaApi(id, LocalDate.of(2026, 11, 12), estado, camion(1, "AA123BB", "EN_RUTA"),
                new ChoferApi(1, "Juan Pérez"), List.of(paradas));
    }

    @BeforeEach
    void datosBase() {
        when(donaciones.entidades()).thenReturn(List.of(
                new EntidadApi(1L, true, "Comedor", new EntidadApi.PersonaJuridica("Fundación Demo"))));
    }

    // ================= camiones =================

    @Test
    void laListaMuestraLaFlotaYSusCifras() throws Exception {
        when(camiones.camiones()).thenReturn(List.of(camion(1, "AA123BB", "DISPONIBLE"), camion(2, "CC456DD", "EN_RUTA")));

        mvc.perform(get("/admin/camiones").with(SesionDePrueba.conRol("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(containsString("AA123BB"), containsString("CC456DD"),
                        containsString("En ruta"), containsString("1500 kg"))));
    }

    @Test
    void sinCamionesInvitaARegistrarElPrimero() throws Exception {
        when(camiones.camiones()).thenReturn(List.of());

        mvc.perform(get("/admin/camiones").with(SesionDePrueba.conRol("ADMIN")))
                .andExpect(content().string(containsString("Registrar el primer camión")));
    }

    @Test
    void crearNormalizaLaPatenteYLlamaALogistica() throws Exception {
        mvc.perform(post("/admin/camiones").with(SesionDePrueba.conRol("ADMIN")).with(csrf())
                        .param("patente", " aa 123 bb ").param("capacidadCargaKg", "1500")
                        .param("capacidadVolumenM3", "20").param("alturaM", "3").param("estadoCamion", "DISPONIBLE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/camiones"))
                .andExpect(flash().attributeExists("toasts"));

        ArgumentCaptor<CamionRequest> pedido = ArgumentCaptor.forClass(CamionRequest.class);
        verify(camiones).crear(pedido.capture());
        assertThat(pedido.getValue().patente()).isEqualTo("AA123BB");
        assertThat(pedido.getValue().estadoCamion()).isEqualTo("DISPONIBLE");
    }

    @Test
    void conDatosInvalidosVuelveAlFormularioYNoLlamaALogistica() throws Exception {
        mvc.perform(post("/admin/camiones").with(SesionDePrueba.conRol("ADMIN")).with(csrf())
                        .param("patente", "").param("capacidadCargaKg", "0").param("capacidadVolumenM3", "abc"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(containsString("Completá este campo."),
                        containsString("Ingresá una cantidad mayor a 0."), containsString("Ingresá un número entero"))));

        verify(camiones, never()).crear(any());
    }

    @Test
    void unaPatenteDuplicadaSeMuestraEnElFormulario() throws Exception {
        when(camiones.crear(any())).thenThrow(new BackendRechazoException(
                ServicioBackend.LOGISTICA, 409, "El dato viola una restricción de la base (valor duplicado)", Map.of()));

        mvc.perform(post("/admin/camiones").with(SesionDePrueba.conRol("ADMIN")).with(csrf())
                        .param("patente", "AA123BB").param("capacidadCargaKg", "1500")
                        .param("capacidadVolumenM3", "20").param("alturaM", "3"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("valor duplicado")));
    }

    @Test
    void unCamionEnRutaNoPermiteCambiarElEstadoNiAunqueLoManden() throws Exception {
        when(camiones.camiones()).thenReturn(List.of(camion(2, "CC456DD", "EN_RUTA")));

        mvc.perform(get("/admin/camiones/2").with(SesionDePrueba.conRol("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(not(containsString("id=\"estadoCamion\"")), containsString("En ruta"))));

        mvc.perform(post("/admin/camiones/2").with(SesionDePrueba.conRol("ADMIN")).with(csrf())
                        .param("patente", "CC456DD").param("capacidadCargaKg", "1500")
                        .param("capacidadVolumenM3", "20").param("alturaM", "3").param("estadoCamion", "DISPONIBLE"))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<CamionRequest> pedido = ArgumentCaptor.forClass(CamionRequest.class);
        verify(camiones).actualizar(eq(2), pedido.capture());
        assertThat(pedido.getValue().estadoCamion()).isNull();
    }

    @Test
    void unCamionDisponibleSiPermiteElegirElEstado() throws Exception {
        when(camiones.camiones()).thenReturn(List.of(camion(1, "AA123BB", "DISPONIBLE")));

        mvc.perform(get("/admin/camiones/1").with(SesionDePrueba.conRol("ADMIN")))
                .andExpect(content().string(containsString("id=\"estadoCamion\"")));
    }

    @Test
    void editarUnCamionInexistenteDaNotFound() throws Exception {
        when(camiones.camiones()).thenReturn(List.of(camion(1, "AA123BB", "DISPONIBLE")));

        mvc.perform(get("/admin/camiones/99").with(SesionDePrueba.conRol("ADMIN"))).andExpect(status().isNotFound());
    }

    @Test
    void siElCamionTieneHistorialLaBajaSeExplicaConUnToastDeError() throws Exception {
        doThrow(new BackendRechazoException(ServicioBackend.LOGISTICA, 409,
                "No se puede eliminar el camión 1: figura en rutas o entregas anteriores", Map.of()))
                .when(camiones).eliminar(1);

        mvc.perform(post("/admin/camiones/1/eliminar").with(SesionDePrueba.conRol("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/camiones"))
                .andExpect(flash().attributeExists("toasts"));
    }

    // ================= seguimiento de la administración =================

    @Test
    void elSeguimientoListaLasRutasConSuProgreso() throws Exception {
        when(rutas.rutas()).thenReturn(List.of(
                ruta(1, "FINALIZADA", parada(1, 1, "Calle Uno", entrega(1, "ENTREGADA", null))),
                ruta(2, "INICIADA", parada(1, 1, "Calle Dos", entrega(2, "EN_TRASLADO", null), entrega(3, "ENTREGADA", null)))));

        mvc.perform(get("/admin/seguimiento").with(SesionDePrueba.conRol("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(containsString("#2"), containsString("#1"),
                        containsString("Juan Pérez"), containsString("1 de 2"))));
    }

    @Test
    void elFiltroPorEstadoMuestraSoloEsasRutas() throws Exception {
        when(rutas.rutas()).thenReturn(List.of(
                ruta(1, "FINALIZADA", parada(1, 1, "Calle Uno", entrega(1, "ENTREGADA", null))),
                ruta(2, "INICIADA", parada(1, 1, "Calle Dos", entrega(2, "EN_TRASLADO", null)))));

        mvc.perform(get("/admin/seguimiento").with(SesionDePrueba.conRol("ADMIN")).param("estado", "INICIADA"))
                .andExpect(content().string(allOf(containsString("#2"), not(containsString(">#1<")))));
    }

    @Test
    void elDetalleMuestraLaEntidadPorNombreYSoloEnlazaLasFotosHttp() throws Exception {
        when(rutas.ruta(2)).thenReturn(ruta(2, "INICIADA",
                parada(1, 1, "Calle Dos", entrega(2, "ENTREGADA", "https://ejemplo.org/f.jpg"),
                        entrega(3, "ENTREGADA", "javascript:alert(1)"))));

        mvc.perform(get("/admin/seguimiento/2").with(SesionDePrueba.conRol("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Fundación Demo"),
                        containsString("Calle Dos 10, Escobar"),
                        containsString("href=\"https://ejemplo.org/f.jpg\""),
                        not(containsString("javascript:alert")),
                        containsString("12/11/2026 14:30"))));
    }

    @Test
    void siDonacionesNoRespondeElDetalleSeVeConElNumeroDeEntidad() throws Exception {
        when(donaciones.entidades()).thenThrow(new BackendNoDisponibleException(ServicioBackend.DONACIONES, 0, null));
        when(rutas.ruta(2)).thenReturn(ruta(2, "PLANIFICADA", parada(1, 5, "Calle Dos", entrega(2, "ASIGNADA_A_RUTA", null))));

        mvc.perform(get("/admin/seguimiento/2").with(SesionDePrueba.conRol("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Entidad 5")));
    }

    @Test
    void soloUnaRutaPlanificadaOfreceIniciarse() throws Exception {
        when(rutas.ruta(2)).thenReturn(ruta(2, "PLANIFICADA", parada(1, 1, "Calle Dos", entrega(2, "ASIGNADA_A_RUTA", null))));
        when(rutas.ruta(3)).thenReturn(ruta(3, "INICIADA", parada(1, 1, "Calle Dos", entrega(3, "EN_TRASLADO", null))));

        mvc.perform(get("/admin/seguimiento/2").with(SesionDePrueba.conRol("ADMIN"))).andExpect(content().string(containsString("Iniciar ruta")));
        mvc.perform(get("/admin/seguimiento/3").with(SesionDePrueba.conRol("ADMIN"))).andExpect(content().string(not(containsString("Iniciar ruta"))));
    }

    @Test
    void unaRutaInexistenteDaNotFound() throws Exception {
        when(rutas.ruta(99)).thenThrow(new RecursoNoEncontradoException(ServicioBackend.LOGISTICA, "No existe la ruta 99"));

        mvc.perform(get("/admin/seguimiento/99").with(SesionDePrueba.conRol("ADMIN"))).andExpect(status().isNotFound());
    }

    @Test
    void iniciarUnaRutaVuelveAlDetalleConUnToast() throws Exception {
        mvc.perform(post("/admin/seguimiento/2/iniciar").with(SesionDePrueba.conRol("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/seguimiento/2"))
                .andExpect(flash().attributeExists("toasts"));

        verify(rutas).iniciar(2);
    }

    @Test
    void siLaRutaYaNoEstaPlanificadaElRechazoSeMuestraComoToast() throws Exception {
        when(rutas.iniciar(2)).thenThrow(new BackendRechazoException(
                ServicioBackend.LOGISTICA, 409, "La ruta 2 no puede iniciarse estando en estado INICIADA", Map.of()));

        mvc.perform(post("/admin/seguimiento/2/iniciar").with(SesionDePrueba.conRol("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/seguimiento/2"))
                .andExpect(flash().attributeExists("toasts"));
    }

    @Test
    void planificarAvisaCuantasRutasSeCrearonOQueNoHabiaPendientes() throws Exception {
        when(rutas.planificar()).thenReturn(List.of(ruta(8, "PLANIFICADA")), List.of());

        mvc.perform(post("/admin/seguimiento/planificar").with(SesionDePrueba.conRol("ADMIN")).with(csrf()))
                .andExpect(redirectedUrl("/admin/seguimiento"))
                .andExpect(flash().attributeExists("toasts"));
        mvc.perform(post("/admin/seguimiento/planificar").with(SesionDePrueba.conRol("ADMIN")).with(csrf()))
                .andExpect(redirectedUrl("/admin/seguimiento"))
                .andExpect(flash().attributeExists("toasts"));
    }

    // ================= seguimiento de la entidad =================

    @Test
    void laEntidadVeSoloSuParadaSinDatosDeLasOtrasNiDelChofer() throws Exception {
        when(rutas.rutas()).thenReturn(List.of(ruta(2, "INICIADA",
                parada(1, 9, "Calle Ajena", entrega(1, "EN_TRASLADO", null)),
                parada(2, 1, "Calle Propia", entrega(2, "EN_TRASLADO", null)))));

        mvc.perform(get("/beneficiaria/seguimiento").with(SesionDePrueba.conRol("BENEFICIARIA")))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Parada 2 de 2"),
                        containsString("Camión AA123BB"),
                        containsString("Donación 102"),
                        not(containsString("Donación 101")),
                        not(containsString("Calle Ajena")),
                        not(containsString("Juan Pérez")))));
    }

    @Test
    void sinEnviosLaEntidadVeUnMensaje() throws Exception {
        when(rutas.rutas()).thenReturn(List.of());

        mvc.perform(get("/beneficiaria/seguimiento").with(SesionDePrueba.conRol("BENEFICIARIA")))
                .andExpect(content().string(containsString("No hay entregas programadas hacia tu organización.")));
    }

    // ================= mapa y simulador =================

    private static RutaApi rutaConCamionUbicado(int id, String estado, boolean simulada, ParadaApi... paradas) {
        CamionApi camion = new CamionApi(1, "AA123BB", 20, 3, 1500, "EN_RUTA", -34.61, -58.39,
                LocalDateTime.of(2026, 11, 12, 17, 30), simulada);
        return new RutaApi(id, LocalDate.of(2026, 11, 12), estado, camion, new ChoferApi(1, "Juan Pérez"), List.of(paradas));
    }

    private static ParadaApi paradaUbicada(int numero, int entidad, double lat, double lng, EntregaDeRutaApi... entregas) {
        return new ParadaApi(numero, numero, entidad,
                new DireccionApi("Calle Dos", "10", new CiudadApi("Escobar"), lat, lng), List.of(entregas));
    }

    @Test
    void laListaDeSeguimientoMuestraElMapaConLosCamionesEnRuta() throws Exception {
        when(rutas.rutas()).thenReturn(List.of(
                rutaConCamionUbicado(2, "INICIADA", true, parada(1, 1, "Calle Dos", entrega(2, "EN_TRASLADO", null)))));

        mvc.perform(get("/admin/seguimiento").with(SesionDePrueba.conRol("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("data-mapa"),
                        containsString("Camión AA123BB"),
                        containsString("Ubicación simulada"),
                        containsString("/admin/seguimiento/ubicaciones.json"))));
    }

    @Test
    void elEndpointDeUbicacionesDevuelveSoloLosCamionesDeRutasEnCurso() throws Exception {
        when(rutas.rutas()).thenReturn(List.of(
                rutaConCamionUbicado(1, "FINALIZADA", false, parada(1, 1, "Calle Uno", entrega(1, "ENTREGADA", null))),
                rutaConCamionUbicado(2, "INICIADA", false, parada(1, 1, "Calle Dos", entrega(2, "EN_TRASLADO", null)))));

        mvc.perform(get("/admin/seguimiento/ubicaciones.json").with(SesionDePrueba.conRol("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].tipo").value("camion"))
                .andExpect(jsonPath("$[0].titulo").value("Camión AA123BB"))
                .andExpect(jsonPath("$[0].lat").value(-34.61))
                .andExpect(jsonPath("$[0].actualizadoEn").value("2026-11-12T17:30Z"));
    }

    @Test
    void elMapaDelDetalleIncluyeElCamionYLasParadasConCoordenadas() throws Exception {
        when(rutas.ruta(2)).thenReturn(rutaConCamionUbicado(2, "INICIADA", false,
                paradaUbicada(1, 1, -34.5, -58.3, entrega(2, "EN_TRASLADO", null))));

        mvc.perform(get("/admin/seguimiento/2/ubicaciones.json").with(SesionDePrueba.conRol("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tipo").value("camion"))
                .andExpect(jsonPath("$[1].tipo").value("sede"))
                .andExpect(jsonPath("$[1].titulo").value("Parada 1: Fundación Demo"));
    }

    @Test
    void elBotonDeSimulacionSoloApareceEnUnaRutaEnCurso() throws Exception {
        when(rutas.ruta(2)).thenReturn(rutaConCamionUbicado(2, "INICIADA", false, parada(1, 1, "Calle Dos", entrega(2, "EN_TRASLADO", null))));
        when(rutas.ruta(3)).thenReturn(ruta(3, "PLANIFICADA", parada(1, 1, "Calle Dos", entrega(3, "ASIGNADA_A_RUTA", null))));

        mvc.perform(get("/admin/seguimiento/2").with(SesionDePrueba.conRol("ADMIN"))).andExpect(content().string(containsString("Simular avance del camión")));
        mvc.perform(get("/admin/seguimiento/3").with(SesionDePrueba.conRol("ADMIN"))).andExpect(content().string(not(containsString("Simular avance del camión"))));
    }

    @Test
    void simularUnPasoAvisaCuantoFaltaYVuelveAlDetalle() throws Exception {
        when(rutas.simularAvance(2)).thenReturn(new SimulacionApi(
                new SimulacionApi.UbicacionApi(1, "AA123BB", -34.61, -58.39, LocalDateTime.of(2026, 11, 12, 17, 30), true),
                2, 7.5, false, true));

        mvc.perform(post("/admin/seguimiento/2/simular").with(SesionDePrueba.conRol("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/seguimiento/2"))
                .andExpect(flash().attributeExists("toasts"));

        verify(rutas).simularAvance(2);
    }

    @Test
    void siElCamionLlegoALaParadaSeAvisa() throws Exception {
        when(rutas.simularAvance(2)).thenReturn(new SimulacionApi(
                new SimulacionApi.UbicacionApi(1, "AA123BB", -34.5, -58.3, LocalDateTime.of(2026, 11, 12, 17, 30), true),
                2, 0.0, true, false));

        mvc.perform(post("/admin/seguimiento/2/simular").with(SesionDePrueba.conRol("ADMIN")).with(csrf()))
                .andExpect(redirectedUrl("/admin/seguimiento/2"))
                .andExpect(flash().attributeExists("toasts"));
    }

    @Test
    void siLaRutaNoEstaEnCursoElRechazoDeLogisticaSeMuestraComoToast() throws Exception {
        when(rutas.simularAvance(2)).thenThrow(new BackendRechazoException(
                ServicioBackend.LOGISTICA, 409, "La ruta 2 no está en curso", Map.of()));

        mvc.perform(post("/admin/seguimiento/2/simular").with(SesionDePrueba.conRol("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/seguimiento/2"))
                .andExpect(flash().attributeExists("toasts"));
    }

    @Test
    void laEntidadVeElMapaSoloConElCamionQueLeLlevaAlgoYSinElChofer() throws Exception {
        when(rutas.rutas()).thenReturn(List.of(
                rutaConCamionUbicado(2, "INICIADA", false,
                        parada(1, 9, "Calle Ajena", entrega(1, "EN_TRASLADO", null)),
                        parada(2, 1, "Calle Propia", entrega(2, "EN_TRASLADO", null))),
                rutaConCamionUbicado(3, "INICIADA", false, parada(1, 9, "Calle Ajena", entrega(3, "EN_TRASLADO", null)))));

        mvc.perform(get("/beneficiaria/seguimiento/ubicaciones.json").with(SesionDePrueba.conRol("BENEFICIARIA")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].detalle").value(containsString("parada 2 de 2")))
                .andExpect(content().string(not(containsString("Juan Pérez"))))
                .andExpect(content().string(not(containsString("Calle Ajena"))));

        mvc.perform(get("/beneficiaria/seguimiento").with(SesionDePrueba.conRol("BENEFICIARIA")))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(containsString("data-mapa"), containsString("Dónde está el camión"))));
    }

    @Test
    void sinCamionesUbicadosElMapaDeLaEntidadQuedaVacioEnVezDeInventarPosiciones() throws Exception {
        when(rutas.rutas()).thenReturn(List.of(
                ruta(2, "INICIADA", parada(1, 1, "Calle Propia", entrega(2, "EN_TRASLADO", null)))));

        mvc.perform(get("/beneficiaria/seguimiento/ubicaciones.json").with(SesionDePrueba.conRol("BENEFICIARIA")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
