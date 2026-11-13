package ar.edu.utn.frba.dds.web.donante;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.utn.frba.dds.web.SesionDePrueba;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.DonacionDelDonante;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Pagina;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Perfil;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteClient;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosClient;
import ar.edu.utn.frba.dds.web.client.logistica.CamionApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.ChoferApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.CiudadApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.DireccionApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.EntregaDeRutaApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.ParadaApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutasClient;
import ar.edu.utn.frba.dds.web.error.BackendNoDisponibleException;
import ar.edu.utn.frba.dds.web.error.ServicioBackend;
import java.math.BigDecimal;
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

@SpringBootTest(properties = "donatrack.web.donante-provisorio-id=1")
@AutoConfigureMockMvc
class SeguimientoDonanteWebTest {

    @Autowired MockMvc mvc;
    @MockitoBean PortalDonanteClient donaciones;
    @MockitoBean IncentivosClient incentivos;
    @MockitoBean RutasClient rutas;

    private final DonacionDelDonante arroz = new DonacionDelDonante(15L, LocalDateTime.of(2026, 11, 10, 15, 0), "Arroz 1 kg",
            BigDecimal.valueOf(20), "KILOGRAMO", "ALIMENTOS", "ARROZ", "EN_TRASLADO", 3L);

    /** Ruta en curso cuyo camión lleva la donación indicada y otra ajena. */
    private static RutaApi rutaQueLleva(long idDonacion, Double lat, Double lng) {
        var camion = new CamionApi(1, "AA123BB", 20, 3, 1500, "EN_RUTA", lat, lng,
                lat == null ? null : LocalDateTime.of(2026, 11, 12, 20, 30), lat == null ? null : true);
        var parada = new ParadaApi(1, 1, 3, new DireccionApi("Calle", "1", new CiudadApi("Escobar"), null, null),
                List.of(new EntregaDeRutaApi(9, (int) idDonacion, "EN_TRASLADO", null, null, List.of()),
                        new EntregaDeRutaApi(10, 999, "EN_TRASLADO", null, null, List.of())));
        return new RutaApi(7, LocalDate.of(2026, 11, 12), "INICIADA", camion, new ChoferApi(1, "Juan"), List.of(parada));
    }

    @BeforeEach
    void datos() {
        when(donaciones.perfil(1)).thenReturn(new Perfil(1L, true, "HUMANA", "Ana", "Pérez", null));
        when(donaciones.resumenPorEstado(1)).thenReturn(Map.of("EN_TRASLADO", 1L, "ASIGNACION_REALIZADA", 2L, "ENTREGADA", 4L));
        when(donaciones.donaciones(eq(1L), anyMap())).thenReturn(new Pagina<>(List.of(arroz), 1, 1, 0));
        when(rutas.rutas()).thenReturn(List.of(rutaQueLleva(15, -34.35, -58.79)));
    }

    @Test
    void muestraElCamionQueLlevaLaDonacionYSusDatos() throws Exception {
        mvc.perform(get("/donante/seguimiento").with(SesionDePrueba.conRol("DONANTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Sección en construcción"))))
                .andExpect(content().string(containsString("Camión AA123BB")))
                .andExpect(content().string(containsString("Lleva 1 de tus donaciones")))
                .andExpect(content().string(containsString("/donante/seguimiento/ubicaciones.json")))
                .andExpect(content().string(containsString("href=\"/donante/donaciones/15\"")))
                .andExpect(content().string(containsString("Asignadas, esperando el camión")));
        verify(donaciones).donaciones(eq(1L), argThat(p -> "EN_TRASLADO".equals(p.get("estado"))));
    }

    @Test
    void elJsonDeRefrescoDevuelveSoloElCamionDelDonante() throws Exception {
        mvc.perform(get("/donante/seguimiento/ubicaciones.json").with(SesionDePrueba.conRol("DONANTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipo").value("camion"))
                .andExpect(jsonPath("$[0].lat").value(-34.35))
                // Del resto de las entregas de la ruta no se dice nada.
                .andExpect(content().string(not(containsString("999"))));
    }

    @Test
    void unCamionSinUbicacionNoSeDibuja() throws Exception {
        when(rutas.rutas()).thenReturn(List.of(rutaQueLleva(15, null, null)));
        mvc.perform(get("/donante/seguimiento/ubicaciones.json").with(SesionDePrueba.conRol("DONANTE")))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void sinDonacionesEnCaminoNoConsultaLogistica() throws Exception {
        when(donaciones.donaciones(eq(1L), anyMap())).thenReturn(new Pagina<>(List.of(), 0, 0, 0));
        mvc.perform(get("/donante/seguimiento").with(SesionDePrueba.conRol("DONANTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ahora no hay donaciones tuyas en camino")))
                .andExpect(content().string(containsString("Tenés 2 donaciones asignadas que todavía no salieron")));
        verify(rutas, never()).rutas();
    }

    @Test
    void siLogisticaNoRespondeMuestraLaListaConUnAviso() throws Exception {
        when(rutas.rutas()).thenThrow(new BackendNoDisponibleException(ServicioBackend.LOGISTICA, 503, null));
        mvc.perform(get("/donante/seguimiento").with(SesionDePrueba.conRol("DONANTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("no pudimos consultar la ubicación de los camiones")))
                .andExpect(content().string(containsString("Arroz 1 kg")));
        mvc.perform(get("/donante/seguimiento/ubicaciones.json").with(SesionDePrueba.conRol("DONANTE"))).andExpect(status().isServiceUnavailable());
    }

    @Test
    void unaPersonaJuridicaTampocoVeIncentivosEnElMenuDeSeguimiento() throws Exception {
        when(donaciones.perfil(1)).thenReturn(new Perfil(1L, true, "JURIDICA", null, null, "Panadería El Sol"));
        mvc.perform(get("/donante/seguimiento").with(SesionDePrueba.conRol("DONANTE")))
                .andExpect(content().string(not(containsString("href=\"/donante/incentivos\""))));
    }
}
