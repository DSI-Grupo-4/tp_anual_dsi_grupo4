package ar.edu.utn.frba.dds.web;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;

/** Sesión OIDC de prueba con uno de los tres roles del realm. No habla con Keycloak. */
public final class SesionDePrueba {

    private SesionDePrueba() {
    }

    public static OidcLoginRequestPostProcessor conRol(String rol) {
        return oidcLogin().idToken(token -> {
            if ("DONANTE".equals(rol)) token.claim("donante_id", "1");
            if ("BENEFICIARIA".equals(rol)) token.claim("entidad_ids", java.util.List.of("1"));
        }).authorities(new SimpleGrantedAuthority("ROLE_" + rol));
    }
}
