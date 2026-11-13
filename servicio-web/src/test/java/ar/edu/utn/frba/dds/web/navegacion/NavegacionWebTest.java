package ar.edu.utn.frba.dds.web.navegacion;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.utn.frba.dds.web.SesionDePrueba;
import ar.edu.utn.frba.dds.web.client.donaciones.DonacionesClient;
import ar.edu.utn.frba.dds.web.client.donaciones.EntidadApi;
import ar.edu.utn.frba.dds.web.client.donaciones.NecesidadesClient;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class NavegacionWebTest {

    @Autowired
    private MockMvc mvc;

    // El panel y las necesidades de la entidad (Persona 5) ya consultan Donaciones: se simula para probar solo la navegación.
    @MockitoBean
    private NecesidadesClient necesidades;

    @MockitoBean
    private DonacionesClient donaciones;

    // El portal del donante (Persona 4) consulta Donaciones e Incentivos.
    @MockitoBean
    private ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteClient portalDonante;

    @MockitoBean
    private ar.edu.utn.frba.dds.web.client.incentivos.IncentivosClient incentivos;

    @BeforeEach
    void clientesSimulados() {
        when(necesidades.entidad(1L)).thenReturn(
                new EntidadApi(1L, true, "Comedor", new EntidadApi.PersonaJuridica("Fundación Demo")));
        when(necesidades.necesidades(1L)).thenReturn(List.of());
        when(donaciones.donacionesDeEntidad(1L)).thenReturn(List.of());
        when(portalDonante.perfil(1L)).thenReturn(new ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Perfil(
                1L, true, "HUMANA", "Ana", "Pérez", null));
    }

    @Test
    void laPortadaUsaElLayoutPublicoConEnlaceParaSaltarAlContenido() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("lang=\"es\""),
                        containsString("class=\"saltar-contenido\" href=\"#contenido\""),
                        containsString("<main id=\"contenido\""),
                        containsString("href=\"/legal/privacidad\""))));
    }

    // El fallback "en construcción" se prueba aparte, en SeccionesPrivadasControllerTest:
    // ya no queda ninguna sección del menú sin controller propio para pegarle acá
    // (ni /admin/donaciones ni /admin/camiones, que era el otro candidato de iam).

    @Test
    void elPanelDeCadaRolMuestraSuMenu() throws Exception {
        mvc.perform(get("/donante").with(SesionDePrueba.conRol("DONANTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("href=\"/donante/incentivos\""),
                        containsString("Donante"),
                        not(containsString("href=\"/admin/camiones\"")))));
        mvc.perform(get("/beneficiaria").with(SesionDePrueba.conRol("BENEFICIARIA"))).andExpect(status().isOk());
    }

    @Test
    void lasMigasLlevanAlPanelYMarcanLaPaginaActual() throws Exception {
        mvc.perform(get("/beneficiaria/necesidades").with(SesionDePrueba.conRol("BENEFICIARIA")))
                .andExpect(content().string(allOf(
                        containsString("<a href=\"/beneficiaria\">Panel</a>"),
                        containsString("<span aria-current=\"page\">Necesidades</span>"))));
    }

    @Test
    void unaSeccionQueNoEstaEnElMenuDa404() throws Exception {
        mvc.perform(get("/admin/inexistente").with(SesionDePrueba.conRol("ADMIN"))).andExpect(status().isNotFound());
        mvc.perform(get("/legal/inexistente")).andExpect(status().isNotFound());
    }

    @Test
    void elCatalogoSoloExisteConElPerfilDev() throws Exception {
        mvc.perform(get("/dev/componentes")).andExpect(status().isNotFound());
    }
}
