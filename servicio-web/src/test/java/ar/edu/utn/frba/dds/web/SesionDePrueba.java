package ar.edu.utn.frba.dds.web;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;

/** Sesión OIDC de prueba con uno de los tres roles del realm. No habla con Keycloak. */
public final class SesionDePrueba {

    private SesionDePrueba() {
    }

    public static OidcLoginRequestPostProcessor conRol(String rol) {
        return oidcLogin().authorities(new SimpleGrantedAuthority("ROLE_" + rol));
    }
}
