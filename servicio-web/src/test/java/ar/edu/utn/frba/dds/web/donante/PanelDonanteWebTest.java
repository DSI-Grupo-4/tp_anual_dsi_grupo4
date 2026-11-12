package ar.edu.utn.frba.dds.web.donante;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.DonacionDelDonante;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Pagina;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Perfil;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteClient;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.Metricas;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosClient;
import ar.edu.utn.frba.dds.web.error.BackendNoDisponibleException;
import ar.edu.utn.frba.dds.web.error.RecursoNoEncontradoException;
import ar.edu.utn.frba.dds.web.error.ServicioBackend;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
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
class PanelDonanteWebTest {

    @Autowired MockMvc mvc;
    @MockitoBean PortalDonanteClient donaciones;
    @MockitoBean IncentivosClient incentivos;

    @BeforeEach
    void datos() {
        when(donaciones.perfil(1)).thenReturn(new Perfil(1L, true, "HUMANA", "Ana", "Pérez", null));
        Map<String, Long> porEstado = new LinkedHashMap<>();
        porEstado.put("EN_DEPOSITO", 1L);
        porEstado.put("EN_TRASLADO", 2L);
        porEstado.put("ENTREGADA", 3L);
        porEstado.put("VENCIDA", 0L);
        when(donaciones.resumenPorEstado(1)).thenReturn(porEstado);
        var arroz = new DonacionDelDonante(9L, LocalDateTime.of(2026, 11, 10, 15, 0), "Arroz 1 kg",
                BigDecimal.valueOf(20), "KILOGRAMO", "ALIMENTOS", "ARROZ", "EN_TRASLADO", 4L);
        when(donaciones.donaciones(eq(1L), anyMap())).thenReturn(new Pagina<>(List.of(arroz), 6, 2, 0));
        when(incentivos.metricas(1, "HISTORICO")).thenReturn(new Metricas("Ana Pérez", "Colaborador", "HISTORICO",
                6, 2, 1, 1, BigDecimal.valueOf(1250), 3, List.of()));
    }

    @Test
    void muestraResumenDeDonacionesEIncentivos() throws Exception {
        mvc.perform(get("/donante"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<main id=\"contenido\"")))
                .andExpect(content().string(not(containsString("Sección en construcción"))))
                .andExpect(content().string(containsString("¡Hola, Ana!")))
                .andExpect(content().string(containsString("href=\"/donante/incentivos\"")))
                .andExpect(content().string(not(containsString("href=\"/admin/camiones\""))))
                .andExpect(content().string(containsString("2 en camino")))
                .andExpect(content().string(containsString("Arroz 1 kg")))
                .andExpect(content().string(containsString("20 kg")))
                .andExpect(content().string(containsString("En traslado")))
                .andExpect(content().string(containsString("Colaborador")))
                .andExpect(content().string(containsString("Impacto acumulado: 1.250")))
                .andExpect(content().string(containsString("Puesto 3 en el ranking")))
                // Los estados con 0 donaciones no se dibujan.
                .andExpect(content().string(not(containsString("donante-barras__barra--VENCIDA"))));
    }

    @Test
    void personaJuridicaNoVeIncentivos() throws Exception {
        when(donaciones.perfil(1)).thenReturn(new Perfil(1L, true, "JURIDICA", null, null, "Panadería El Sol"));
        mvc.perform(get("/donante"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("¡Hola, Panadería El Sol!")))
                .andExpect(content().string(not(containsString("Impacto acumulado"))))
                // Requerimiento 7: Incentivos no aparece en el menú de una persona jurídica.
                .andExpect(content().string(not(containsString("href=\"/donante/incentivos\""))))
                .andExpect(content().string(containsString("href=\"/donante/donaciones\"")));
        verify(incentivos, never()).metricas(anyLong(), anyString());
    }

    @Test
    void siIncentivosNoRespondeElPanelSeMuestraConAviso() throws Exception {
        when(incentivos.metricas(1, "HISTORICO"))
                .thenThrow(new BackendNoDisponibleException(ServicioBackend.INCENTIVOS, 503, null));
        mvc.perform(get("/donante"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("no pudimos consultar tus incentivos")))
                .andExpect(content().string(containsString("Arroz 1 kg")));
    }

    @Test
    void sinDonacionesMuestraEstadoVacioYNoPideLaLista() throws Exception {
        when(donaciones.resumenPorEstado(1)).thenReturn(Map.of("EN_DEPOSITO", 0L));
        mvc.perform(get("/donante"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Todavía no registraste donaciones.")));
        verify(donaciones, never()).donaciones(anyLong(), anyMap());
    }

    @Test
    void escapaTextosQueVienenDelBackend() throws Exception {
        when(donaciones.perfil(1)).thenReturn(new Perfil(1L, true, "HUMANA", "<script>x</script>", "Pérez", null));
        mvc.perform(get("/donante"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>x</script>"))));
    }

    @Test
    void sinDonanteVinculadoExplicaQuePasaEnLugarDelError404Generico() throws Exception {
        when(donaciones.perfil(1)).thenThrow(new RecursoNoEncontradoException(ServicioBackend.DONACIONES, "No existe el donante 1"));
        mvc.perform(get("/donante"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Tu cuenta todavía no está vinculada a un donante")))
                .andExpect(content().string(containsString("href=\"/donante/incentivos\"")))
                .andExpect(content().string(not(containsString("No encontramos esta página"))));
    }

    @Test
    void lasDonacionesRecientesEnlazanAlDetalle() throws Exception {
        mvc.perform(get("/donante"))
                .andExpect(content().string(containsString("href=\"/donante/donaciones/9\"")));
    }
}
