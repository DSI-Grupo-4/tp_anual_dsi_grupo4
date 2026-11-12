package ar.edu.utn.frba.dds.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AccesoWebTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void unAnonimoEnZonaPrivadaVaAlLoginYLoPublicoSigueAbierto() throws Exception {
        mvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/oauth2/authorization/keycloak")));
        mvc.perform(get("/donante"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/oauth2/authorization/keycloak")));
        mvc.perform(get("/beneficiaria"))
                .andExpect(status().is3xxRedirection());
        mvc.perform(get("/ingresar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/oauth2/authorization/keycloak")));
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/ayuda")).andExpect(status().isOk());
    }

    @Test
    void unRolEnLaZonaDeOtroRecibe403() throws Exception {
        mvc.perform(get("/admin").with(SesionDePrueba.conRol("DONANTE")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/donante").with(SesionDePrueba.conRol("ADMIN")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/beneficiaria/necesidades").with(SesionDePrueba.conRol("DONANTE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void unAdminEntraASuZona() throws Exception {
        mvc.perform(get("/admin/camiones").with(SesionDePrueba.conRol("ADMIN")))
                .andExpect(status().isOk());
        mvc.perform(get("/donante").with(SesionDePrueba.conRol("DONANTE")))
                .andExpect(status().isOk());
        mvc.perform(get("/beneficiaria").with(SesionDePrueba.conRol("BENEFICIARIA")))
                .andExpect(status().isOk());
    }
}
