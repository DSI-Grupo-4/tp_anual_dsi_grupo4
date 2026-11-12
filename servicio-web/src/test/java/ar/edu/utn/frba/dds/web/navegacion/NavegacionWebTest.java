package ar.edu.utn.frba.dds.web.navegacion;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class NavegacionWebTest {

    @Autowired
    private MockMvc mvc;

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

    @Test
    void unaSeccionPrivadaSinImplementarMuestraElMenuDelRolYMarcaLaActiva() throws Exception {
        mvc.perform(get("/admin/camiones"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Sección en construcción"),
                        containsString("href=\"/admin/camiones\""),
                        containsString("aria-current=\"page\""),
                        not(containsString("href=\"/donante/incentivos\"")))));
    }

    @Test
    void elPanelDeCadaRolMuestraSuMenu() throws Exception {
        mvc.perform(get("/donante"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("href=\"/donante/incentivos\""),
                        containsString("Donante"),
                        not(containsString("href=\"/admin/camiones\"")))));
        mvc.perform(get("/beneficiaria")).andExpect(status().isOk());
    }

    @Test
    void lasMigasLlevanAlPanelYMarcanLaPaginaActual() throws Exception {
        mvc.perform(get("/beneficiaria/necesidades"))
                .andExpect(content().string(allOf(
                        containsString("<a href=\"/beneficiaria\">Panel</a>"),
                        containsString("<span aria-current=\"page\">Necesidades</span>"))));
    }

    @Test
    void unaSeccionQueNoEstaEnElMenuDa404() throws Exception {
        mvc.perform(get("/admin/inexistente")).andExpect(status().isNotFound());
        mvc.perform(get("/legal/inexistente")).andExpect(status().isNotFound());
    }

    @Test
    void elCatalogoSoloExisteConElPerfilDev() throws Exception {
        mvc.perform(get("/dev/componentes")).andExpect(status().isNotFound());
    }
}
