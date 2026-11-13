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
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.CamionEnCurso;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.DonacionDelDonante;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Pagina;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Perfil;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteClient;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosClient;
import ar.edu.utn.frba.dds.web.client.logistica.RutasClient;
import ar.edu.utn.frba.dds.web.error.BackendNoDisponibleException;
import ar.edu.utn.frba.dds.web.error.ServicioBackend;
import java.math.BigDecimal;
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

    private static CamionEnCurso camion(Double lat, Double lng) {
        return new CamionEnCurso("AA123BB", lat, lng,
                lat == null ? null : LocalDateTime.of(2026, 11, 12, 20, 30), lat == null ? null : true, 1);
    }

    @BeforeEach
    void datos() {
        when(donaciones.perfil(1)).thenReturn(new Perfil(1L, true, "HUMANA", "Ana", "Pérez", null));
        when(donaciones.resumenPorEstado(1)).thenReturn(Map.of("EN_TRASLADO", 1L, "ASIGNACION_REALIZADA", 2L, "ENTREGADA", 4L));
        when(donaciones.donaciones(eq(1L), anyMap())).thenReturn(new Pagina<>(List.of(arroz), 1, 1, 0));
        when(donaciones.camiones(1)).thenReturn(List.of(camion(-34.35, -58.79)));
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
        when(donaciones.camiones(1)).thenReturn(List.of(camion(null, null)));
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
        verify(donaciones, never()).camiones(1);
    }

    @Test
    void siLogisticaNoRespondeMuestraLaListaConUnAviso() throws Exception {
        when(donaciones.camiones(1)).thenThrow(new BackendNoDisponibleException(ServicioBackend.LOGISTICA, 503, null));
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
