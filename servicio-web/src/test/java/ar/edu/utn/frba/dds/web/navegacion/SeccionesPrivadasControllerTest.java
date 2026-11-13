package ar.edu.utn.frba.dds.web.navegacion;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.utn.frba.dds.web.controller.SeccionesPrivadasController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

// Slice test: solo carga SeccionesPrivadasController, sin los controllers reales de
// admin/donante/beneficiaria. Así /admin/donaciones siempre cae en el fallback acá,
// sin importar qué secciones ya haya implementado cada persona en el contexto completo.
// La protección por rol de /admin/** ya se prueba en AccesoWebTest: acá se desactivan
// los filtros de seguridad para no depender de esa configuración.
@WebMvcTest(controllers = SeccionesPrivadasController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({NavegacionModelAdvice.class, NavegacionService.class})
class SeccionesPrivadasControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void unaSeccionPrivadaSinImplementarMuestraElMenuDelRolYMarcaLaActiva() throws Exception {
        mvc.perform(get("/admin/donaciones"))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Sección en construcción"),
                        containsString("href=\"/admin/camiones\""),
                        containsString("href=\"/admin/donaciones\"\n             aria-current=\"page\""),
                        not(containsString("href=\"/donante/incentivos\"")))));
    }
}
