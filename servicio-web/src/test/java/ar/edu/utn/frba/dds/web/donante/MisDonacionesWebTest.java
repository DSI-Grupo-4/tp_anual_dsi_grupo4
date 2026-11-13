package ar.edu.utn.frba.dds.web.donante;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.utn.frba.dds.web.SesionDePrueba;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Bien;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.CambioEstado;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.DetalleDonacion;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.DonacionDelDonante;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Entidad;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Pagina;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.PersonaJuridica;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Perfil;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteClient;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosClient;
import ar.edu.utn.frba.dds.web.error.BackendNoDisponibleException;
import ar.edu.utn.frba.dds.web.error.RecursoNoEncontradoException;
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

@SpringBootTest(properties = "donatrack.web.donante-provisorio-id=1")
@AutoConfigureMockMvc
class MisDonacionesWebTest {

    @Autowired MockMvc mvc;
    @MockitoBean PortalDonanteClient donaciones;
    @MockitoBean IncentivosClient incentivos;

    DonacionDelDonante arroz = new DonacionDelDonante(9L, LocalDateTime.of(2026, 11, 10, 15, 0), "Arroz 1 kg",
            BigDecimal.valueOf(20), "KILOGRAMO", "ALIMENTOS", "ARROZ", "EN_TRASLADO", 4L);

    @BeforeEach
    void datos() {
        when(donaciones.perfil(1)).thenReturn(new Perfil(1L, true, "HUMANA", "Ana", "Pérez", null));
        when(donaciones.donaciones(eq(1L), anyMap())).thenReturn(new Pagina<>(List.of(arroz), 23, 3, 0));
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> parametrosEnviados() {
        ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        verify(donaciones).donaciones(eq(1L), captor.capture());
        return captor.getValue();
    }

    @Test
    void listaConFiltrosYPaginacion() throws Exception {
        mvc.perform(get("/donante/donaciones").with(SesionDePrueba.conRol("DONANTE")).param("estado", "EN_TRASLADO").param("categoria", "ALIMENTOS"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Sección en construcción"))))
                .andExpect(content().string(containsString("Arroz 1 kg")))
                .andExpect(content().string(containsString("href=\"/donante/donaciones/9\"")))
                .andExpect(content().string(containsString("Mostrando 1–1 de 23")))
                .andExpect(content().string(containsString("Página 1 de 3")))
                // El enlace a la página siguiente conserva los filtros.
                .andExpect(content().string(containsString("/donante/donaciones?estado=EN_TRASLADO&amp;categoria=ALIMENTOS&amp;page=1")))
                .andExpect(content().string(containsString("Quitar filtros")));
        assertParametros(Map.of("estado", "EN_TRASLADO", "categoria", "ALIMENTOS", "page", "0", "size", "10"));
    }

    @Test
    void descartaFiltrosInvalidosEnLugarDeMandarlosALaApi() throws Exception {
        mvc.perform(get("/donante/donaciones").with(SesionDePrueba.conRol("DONANTE"))
                        .param("estado", "INVENTADO").param("categoria", "ALIMENTOS").param("subcategoria", "SILLA")
                        .param("page", "-3"))
                .andExpect(status().isOk());
        // SILLA no es de ALIMENTOS: se queda solo la categoría.
        assertParametros(Map.of("categoria", "ALIMENTOS", "page", "0", "size", "10"));
    }

    @Test
    void buscaPorTexto() throws Exception {
        mvc.perform(get("/donante/donaciones").with(SesionDePrueba.conRol("DONANTE")).param("q", "  arroz  ").param("page", "abc")).andExpect(status().isOk());
        assertParametros(Map.of("q", "arroz", "page", "0", "size", "10"));
    }

    private void assertParametros(Map<String, String> esperados) {
        org.assertj.core.api.Assertions.assertThat(parametrosEnviados()).containsExactlyInAnyOrderEntriesOf(esperados);
    }

    @Test
    void sinDonacionesNiFiltrosMuestraElEstadoVacio() throws Exception {
        when(donaciones.donaciones(eq(1L), anyMap())).thenReturn(new Pagina<>(List.of(), 0, 0, 0));
        mvc.perform(get("/donante/donaciones").with(SesionDePrueba.conRol("DONANTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Todavía no registraste donaciones")))
                .andExpect(content().string(not(containsString("Quitar filtros"))));
    }

    @Test
    void sinResultadosConFiltrosOfreceQuitarlos() throws Exception {
        when(donaciones.donaciones(eq(1L), anyMap())).thenReturn(new Pagina<>(List.of(), 0, 0, 0));
        mvc.perform(get("/donante/donaciones").with(SesionDePrueba.conRol("DONANTE")).param("estado", "VENCIDA"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No encontramos donaciones con esos filtros")));
    }

    @Test
    void donanteInexistenteMuestraLaCuentaSinVincular() throws Exception {
        when(donaciones.perfil(1))
                .thenThrow(new RecursoNoEncontradoException(ServicioBackend.DONACIONES, "No existe el donante 1"));
        mvc.perform(get("/donante/donaciones").with(SesionDePrueba.conRol("DONANTE")))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Tu cuenta todavía no está vinculada a un donante")));
    }

    @Test
    void detalleConBienesEntidadEHistorialDelMasRecienteAlMasViejo() throws Exception {
        var bien = new Bien("Arroz largo fino", "ALIMENTOS", "ARROZ", "KILOGRAMO", BigDecimal.valueOf(20), null, "2027-01-10");
        when(donaciones.donacion(1, 9)).thenReturn(new DetalleDonacion(9L, 1L, LocalDateTime.of(2026, 11, 10, 15, 0),
                "Arroz 1 kg", BigDecimal.valueOf(20), "KILOGRAMO", "ALIMENTOS", "ARROZ", "EN_TRASLADO", 4L, List.of(bien)));
        when(donaciones.historial(1, 9)).thenReturn(List.of(
                new CambioEstado("EN_DEPOSITO", LocalDateTime.of(2026, 11, 10, 15, 0), "Recibida en el depósito"),
                new CambioEstado("EN_TRASLADO", LocalDateTime.of(2026, 11, 11, 9, 0), null)));
        when(donaciones.entidad(4)).thenReturn(new Entidad(4L, true, "Comedor", new PersonaJuridica("Comedor Sonrisas", "ONG", "Alimentos"), null));

        String html = mvc.perform(get("/donante/donaciones/9").with(SesionDePrueba.conRol("DONANTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Donación #9")))
                .andExpect(content().string(containsString("Arroz largo fino")))
                .andExpect(content().string(containsString("Vence")))
                .andExpect(content().string(containsString("Comedor Sonrisas")))
                .andExpect(content().string(containsString("Recibida en el depósito")))
                .andExpect(content().string(containsString("Ver en el mapa de seguimiento")))
                .andExpect(content().string(containsString("<a href=\"/donante/donaciones\">Mis donaciones</a>")))
                .andReturn().getResponse().getContentAsString();
        String historial = html.substring(html.indexOf("donante-historial"));
        org.assertj.core.api.Assertions.assertThat(historial.indexOf("En traslado"))
                .isLessThan(historial.indexOf("En depósito"));
    }

    @Test
    void siLaEntidadNoRespondeElDetalleSeMuestraIgual() throws Exception {
        when(donaciones.donacion(1, 9)).thenReturn(new DetalleDonacion(9L, 1L, null, "Arroz 1 kg", BigDecimal.ONE,
                "KILOGRAMO", "ALIMENTOS", "ARROZ", "ENTREGADA", 4L, null));
        when(donaciones.historial(1, 9)).thenReturn(List.of());
        when(donaciones.entidad(anyLong())).thenThrow(new BackendNoDisponibleException(ServicioBackend.DONACIONES, 503, null));
        mvc.perform(get("/donante/donaciones/9").with(SesionDePrueba.conRol("DONANTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Entidad #4")))
                .andExpect(content().string(not(containsString("Ver en el mapa de seguimiento"))));
    }

    @Test
    void unaDonacionAjenaOInexistenteDa404() throws Exception {
        when(donaciones.donacion(1, 77))
                .thenThrow(new RecursoNoEncontradoException(ServicioBackend.DONACIONES, "No existe la donación 77 del donante 1"));
        mvc.perform(get("/donante/donaciones/77").with(SesionDePrueba.conRol("DONANTE"))).andExpect(status().isNotFound());
    }
}
