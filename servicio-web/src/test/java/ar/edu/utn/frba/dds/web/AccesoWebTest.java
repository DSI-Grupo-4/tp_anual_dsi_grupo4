package ar.edu.utn.frba.dds.web;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
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
    void crearCuentaAbreElRegistroDeKeycloakYIngresarElLogin() throws Exception {
        mvc.perform(get("/oauth2/authorization/keycloak").param("registro", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", allOf(
                        containsString("/realms/donatrack/protocol/openid-connect/auth"),
                        containsString("prompt=create"))));
        mvc.perform(get("/oauth2/authorization/keycloak"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", not(containsString("prompt="))));
    }

    @Test
    void unaCuentaSinRolVeLaVinculacionYUnAnonimoVaAlLogin() throws Exception {
        mvc.perform(get("/cuenta"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("/oauth2/authorization/keycloak")));
        mvc.perform(get("/cuenta").with(oidcLogin()))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Tu cuenta todavía no está vinculada"),
                        containsString("action=\"/cuenta/vincular\""),
                        containsString("action=\"/cuenta/donante\""),
                        containsString("action=\"/cuenta/donante-juridico\""),
                        containsString("action=\"/cuenta/entidad\""))));
        mvc.perform(get("/cuenta").with(SesionDePrueba.conRol("DONANTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("href=\"/donante\""),
                        containsString("action=\"/cuenta/entidad\""),
                        not(containsString("action=\"/cuenta/donante\"")),
                        not(containsString("action=\"/cuenta/donante-juridico\"")))));
        mvc.perform(post("/cuenta/donante").with(oidcLogin()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cuenta"));
    }

    @Test
    void elLogoutBorraLaCookieDeSesion() throws Exception {
        mvc.perform(post("/logout").with(SesionDePrueba.conRol("ADMIN")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(cookie().maxAge("DONATRACK_SESSION", 0));
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
