package ar.edu.utn.frba.dds.web.publico;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PaginasPublicasWebTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private FuentePublica fuente;

    private static final DonacionPublica ARROZ = new DonacionPublica("Arroz 1 kg", "ALIMENTOS", "ARROZ", "20", "KILOGRAMO",
            "Comedor Escobar Sonrisas", LocalDateTime.of(2026, 11, 3, 17, 30), "https://f/arroz.jpg");
    private static final DonacionPublica SILLAS = new DonacionPublica(null, "MOBILIARIO", "SILLA", "12", "UNIDAD",
            null, LocalDateTime.of(2026, 11, 4, 1, 0), null);

    @Test
    void laLandingMuestraPropositoCifrasDestacadasYLosTresAccesosQuePideLaConsigna() throws Exception {
        when(fuente.resumen()).thenReturn(new ResumenPublico(new CifrasPublicas(8, 2, 34, null), List.of(ARROZ, SILLAS), true));

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Transformá vidas a través de la donación"),
                        containsString(">8<"),
                        containsString("Dato no disponible en este momento"),
                        containsString("Arroz 1 kg"),
                        containsString("alt=\"Foto de la entrega de Arroz 1 kg a Comedor Escobar Sonrisas\""),
                        containsString("Alimentos"),
                        containsString("20 kg"),
                        containsString("03/11/2026"),
                        containsString("Sillas"),
                        containsString("una entidad beneficiaria"),
                        containsString("Sin foto de la entrega"),
                        containsString("href=\"/galeria\""),
                        containsString("href=\"/registro\""),
                        containsString("href=\"/legal/privacidad\""))));
    }

    @Test
    void laLandingAvisaSiNoSePudieronCargarLasEntregas() throws Exception {
        when(fuente.resumen()).thenReturn(new ResumenPublico(new CifrasPublicas(null, null, 3, 5), List.of(), false));

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("No pudimos cargar las entregas recientes"),
                        not(containsString("Todavía no hay entregas confirmadas")))));
    }

    @Test
    void laLandingSinEntregasDelMesLoDiceSinMostrarError() throws Exception {
        when(fuente.resumen()).thenReturn(new ResumenPublico(new CifrasPublicas(0, 0, 3, 5), List.of(), true));

        mvc.perform(get("/"))
                .andExpect(content().string(allOf(
                        containsString("Todavía no hay entregas confirmadas en los últimos 30 días"),
                        not(containsString("No pudimos cargar")))));
    }

    @Test
    void laGaleriaPaginaConEnlacesYEstadosDeshabilitados() throws Exception {
        when(fuente.fotos(2, 12)).thenReturn(new PaginaFotos(List.of(ARROZ), 2, 3, 25));

        mvc.perform(get("/galeria").param("pagina", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("25 fotos"),
                        containsString("Página 2 de 3"),
                        containsString("href=\"/galeria?pagina=1\""),
                        containsString("href=\"/galeria?pagina=3\""),
                        containsString("src=\"https://f/arroz.jpg\""))));
    }

    @Test
    void laGaleriaVaciaLoExplica() throws Exception {
        when(fuente.fotos(1, 12)).thenReturn(new PaginaFotos(List.of(), 1, 1, 0));

        mvc.perform(get("/galeria"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Todavía no hay fotos de entregas.")));
    }

    @Test
    void elRegistroExplicaElProcesoYLlevaAlRegistroConfigurado() throws Exception {
        mvc.perform(get("/registro"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("role=\"tablist\""),
                        containsString("Para entidades beneficiarias"),
                        containsString("dirección de la sede"),
                        containsString("<a class=\"boton boton--primario\" href=\"/oauth2/authorization/keycloak?registro\">Crear cuenta</a>"),
                        containsString("href=\"/legal/terminos\""))));
    }

    @Test
    void lasPaginasLegalesYDeAyudaEstanPublicadas() throws Exception {
        mvc.perform(get("/legal/privacidad")).andExpect(status().isOk())
                .andExpect(content().string(allOf(containsString("Ley 25.326"), containsString("AGENCIA DE ACCESO A LA INFORMACIÓN PÚBLICA"))));
        mvc.perform(get("/legal/terminos")).andExpect(status().isOk())
                .andExpect(content().string(allOf(containsString("Ley 26.653"), containsString("WCAG 2.1"))));
        mvc.perform(get("/ayuda")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Estados de una donación")));
    }
}
