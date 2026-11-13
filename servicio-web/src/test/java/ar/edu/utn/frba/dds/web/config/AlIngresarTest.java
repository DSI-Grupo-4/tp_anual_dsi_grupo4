package ar.edu.utn.frba.dds.web.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

import static org.assertj.core.api.Assertions.assertThat;

class AlIngresarTest {
    @Test
    void ingresoDirectoVaAlPanelDelRolOSuCuenta() throws Exception {
        var handler = new SeguridadConfig.AlIngresar();
        for (String rol : new String[]{"ADMIN", "DONANTE", "BENEFICIARIA", ""}) {
            var request = new MockHttpServletRequest();
            var response = new MockHttpServletResponse();
            var auth = new TestingAuthenticationToken("usuario", "", rol.isEmpty() ? "OIDC_USER" : "ROLE_" + rol);
            handler.onAuthenticationSuccess(request, response, auth);
            assertThat(response.getRedirectedUrl()).isEqualTo(rol.isEmpty() ? "/cuenta" : "/" + rol.toLowerCase(java.util.Locale.ROOT));
        }
    }

    @Test
    void conservaLaPaginaPrivadaSolicitadaAntesDelLogin() throws Exception {
        var request = new MockHttpServletRequest("GET", "/donante/donaciones");
        var response = new MockHttpServletResponse();
        new HttpSessionRequestCache().saveRequest(request, response);
        new SeguridadConfig.AlIngresar().onAuthenticationSuccess(request, response,
                new TestingAuthenticationToken("usuario", "", "ROLE_DONANTE"));
        assertThat(response.getRedirectedUrl()).contains("/donante/donaciones");
    }
}
