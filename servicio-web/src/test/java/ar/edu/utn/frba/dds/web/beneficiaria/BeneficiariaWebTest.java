package ar.edu.utn.frba.dds.web.beneficiaria;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.utn.frba.dds.web.client.donaciones.DonacionApi;
import ar.edu.utn.frba.dds.web.client.donaciones.DonacionesClient;
import ar.edu.utn.frba.dds.web.client.donaciones.EntidadApi;
import ar.edu.utn.frba.dds.web.client.donaciones.HistorialEstadoApi;
import ar.edu.utn.frba.dds.web.client.donaciones.NecesidadApi;
import ar.edu.utn.frba.dds.web.client.donaciones.NecesidadRequest;
import ar.edu.utn.frba.dds.web.client.donaciones.NecesidadesClient;
import ar.edu.utn.frba.dds.web.error.BackendRechazoException;
import ar.edu.utn.frba.dds.web.error.ServicioBackend;
import java.math.BigDecimal;
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

/** Pantallas del portal de la entidad, con Donaciones simulado. La entidad de las pruebas es la 1 (valor por defecto). */
@SpringBootTest
@AutoConfigureMockMvc
class BeneficiariaWebTest {

    private static final EntidadApi ENTIDAD =
            new EntidadApi(1L, true, "Comedor", new EntidadApi.PersonaJuridica("Fundación Demo"));

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private NecesidadesClient necesidades;

    @MockitoBean
    private DonacionesClient donaciones;

    private static DonacionApi donacion(long id, String estado, Long entidad) {
        return new DonacionApi(id, "Arroz " + id, new BigDecimal("20.00"), "KILOGRAMO", "ALIMENTOS", "ARROZ", estado, entidad);
    }

    private static NecesidadApi necesidad(long id, boolean recurrente, boolean satisfecha) {
        return new NecesidadApi(id, recurrente ? "RECURRENTE" : "EXTRAORDINARIA", "Leche " + id, "FIDEOS_SECOS",
                "KILOGRAMO", new BigDecimal("50"), new BigDecimal("10"), satisfecha,
                recurrente ? "MENSUAL" : null, recurrente ? null : "INUNDACION");
    }

    @BeforeEach
    void datosBase() {
        when(necesidades.entidad(1L)).thenReturn(ENTIDAD);
        when(donaciones.donaciones()).thenReturn(List.of(
                donacion(1, "ENTREGADA", 1L), donacion(2, "EN_TRASLADO", 1L), donacion(3, "EN_TRASLADO", 2L)));
    }

    // ---- panel ----

    @Test
    void elPanelSaludaALaEntidadYCuentaSoloSusDonaciones() throws Exception {
        mvc.perform(get("/beneficiaria"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Fundación Demo"),
                        containsString("Arroz 1"),
                        containsString("Arroz 2"),
                        not(containsString("Arroz 3")))));
    }

    @Test
    void siDonacionesNoRespondeElPanelMuestraLaPaginaDeServicioNoDisponible() throws Exception {
        when(donaciones.donaciones()).thenThrow(new ar.edu.utn.frba.dds.web.error.BackendNoDisponibleException(
                ServicioBackend.DONACIONES, 0, null));

        mvc.perform(get("/beneficiaria")).andExpect(status().isServiceUnavailable());
    }

    // ---- necesidades ----

    @Test
    void laListaMuestraLasNecesidadesYLasCifras() throws Exception {
        when(necesidades.necesidades(1L)).thenReturn(List.of(necesidad(5, true, false), necesidad(6, false, false)));

        mvc.perform(get("/beneficiaria/necesidades"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Leche 5"),
                        containsString("Recurrente · Mensual"),
                        containsString("Extraordinaria · Inundación"),
                        containsString("10 / 50"))));
    }

    @Test
    void sinNecesidadesInvitaARegistrarLaPrimera() throws Exception {
        when(necesidades.necesidades(1L)).thenReturn(List.of());

        mvc.perform(get("/beneficiaria/necesidades"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Registrar la primera necesidad")));
    }

    @Test
    void elFormularioDeUnaRecurrentePideLaPeriodicidadYElDeUnaExtraordinariaLaSituacion() throws Exception {
        mvc.perform(get("/beneficiaria/necesidades/nueva").param("tipo", "RECURRENTE"))
                .andExpect(content().string(allOf(containsString("id=\"periodicidad\""), not(containsString("id=\"tipoExtraordinario\"")))));
        mvc.perform(get("/beneficiaria/necesidades/nueva").param("tipo", "EXTRAORDINARIA"))
                .andExpect(content().string(allOf(containsString("id=\"tipoExtraordinario\""), not(containsString("id=\"periodicidad\"")))));
    }

    @Test
    void crearUnaNecesidadValidaLlamaADonacionesYVuelveALaListaConUnToast() throws Exception {
        mvc.perform(post("/beneficiaria/necesidades")
                        .param("tipo", "RECURRENTE").param("descripcion", "  Leche en polvo ")
                        .param("subcategoria", "FIDEOS_SECOS").param("unidadMedida", "KILOGRAMO")
                        .param("cantidadRequerida", "50").param("periodicidad", "MENSUAL"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/beneficiaria/necesidades"))
                .andExpect(flash().attributeExists("toasts"));

        ArgumentCaptor<NecesidadRequest> pedido = ArgumentCaptor.forClass(NecesidadRequest.class);
        verify(necesidades).crear(eq(1L), pedido.capture());
        org.assertj.core.api.Assertions.assertThat(pedido.getValue().descripcion()).isEqualTo("Leche en polvo");
        org.assertj.core.api.Assertions.assertThat(pedido.getValue().tipoExtraordinario()).isNull();
    }

    @Test
    void siFaltanDatosVuelveAlFormularioMarcandoLosCamposYNoLlamaADonaciones() throws Exception {
        mvc.perform(post("/beneficiaria/necesidades")
                        .param("tipo", "RECURRENTE").param("descripcion", "").param("cantidadRequerida", "-3"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Completá este campo."),
                        containsString("Ingresá una cantidad mayor a 0."),
                        containsString("aria-invalid=\"true\""))));

        verify(necesidades, never()).crear(any(), any());
    }

    @Test
    void unaCantidadQueNoEsUnNumeroSeExplicaEnElCampo() throws Exception {
        mvc.perform(post("/beneficiaria/necesidades")
                        .param("tipo", "RECURRENTE").param("descripcion", "Leche").param("subcategoria", "FIDEOS_SECOS")
                        .param("unidadMedida", "KILOGRAMO").param("cantidadRequerida", "mucho").param("periodicidad", "MENSUAL"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ingresá un número válido")));
    }

    @Test
    void unRechazoDeDonacionesSeMuestraEnElFormulario() throws Exception {
        when(necesidades.crear(eq(1L), any())).thenThrow(new BackendRechazoException(
                ServicioBackend.DONACIONES, 400, "Datos inválidos", Map.of("descripcion", "ya existe una necesidad igual")));

        mvc.perform(post("/beneficiaria/necesidades")
                        .param("tipo", "RECURRENTE").param("descripcion", "Leche").param("subcategoria", "FIDEOS_SECOS")
                        .param("unidadMedida", "KILOGRAMO").param("cantidadRequerida", "5").param("periodicidad", "MENSUAL"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("ya existe una necesidad igual")));
    }

    @Test
    void editarUnaNecesidadAjenaOInexistenteDaNotFound() throws Exception {
        when(necesidades.necesidades(1L)).thenReturn(List.of(necesidad(5, true, false)));

        mvc.perform(get("/beneficiaria/necesidades/99")).andExpect(status().isNotFound());
    }

    @Test
    void editarMuestraLosDatosActuales() throws Exception {
        when(necesidades.necesidades(1L)).thenReturn(List.of(necesidad(5, true, false)));

        mvc.perform(get("/beneficiaria/necesidades/5"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(containsString("Leche 5"), containsString("Editar necesidad"))));
    }

    @Test
    void siLaNecesidadTieneDonacionesAsignadasLaBajaSeExplicaConUnToastDeError() throws Exception {
        org.mockito.Mockito.doThrow(new BackendRechazoException(
                ServicioBackend.DONACIONES, 409, "La necesidad tiene donaciones asignadas", Map.of()))
                .when(necesidades).eliminar(1L, 5L);

        mvc.perform(post("/beneficiaria/necesidades/5/eliminar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/beneficiaria/necesidades"))
                .andExpect(flash().attributeExists("toasts"));
    }

    // ---- donaciones asignadas ----

    @Test
    void elFiltroDePendientesNoMuestraLasRecibidas() throws Exception {
        mvc.perform(get("/beneficiaria/donaciones").param("filtro", "pendientes"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(containsString("Arroz 2"), not(containsString("Arroz 1")))));
    }

    @Test
    void unFiltroDesconocidoMuestraTodasLasDeLaEntidad() throws Exception {
        mvc.perform(get("/beneficiaria/donaciones").param("filtro", "cualquiera"))
                .andExpect(content().string(allOf(containsString("Arroz 1"), containsString("Arroz 2"), not(containsString("Arroz 3")))));
    }

    @Test
    void elDetalleMuestraElHistorialEnHoraLocal() throws Exception {
        when(donaciones.donacion(2L)).thenReturn(donacion(2, "EN_TRASLADO", 1L));
        when(donaciones.historial(2L)).thenReturn(List.of(
                new HistorialEstadoApi("EN_TRASLADO", LocalDateTime.of(2026, 11, 3, 17, 30), "La ruta salió del depósito")));

        mvc.perform(get("/beneficiaria/donaciones/2"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("La ruta salió del depósito"),
                        containsString("03/11/2026 14:30"))));
    }

    @Test
    void unaDonacionDeOtraEntidadSeTrataComoInexistente() throws Exception {
        when(donaciones.donacion(3L)).thenReturn(donacion(3, "EN_TRASLADO", 2L));

        mvc.perform(get("/beneficiaria/donaciones/3")).andExpect(status().isNotFound());
    }
}
