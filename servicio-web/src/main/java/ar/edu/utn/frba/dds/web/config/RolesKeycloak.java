package ar.edu.utn.frba.dds.web.config;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;

/**
 * Traduce los roles del realm donatrack a ROLE_ADMIN, ROLE_DONANTE y ROLE_BENEFICIARIA.
 * Keycloak los manda en el claim realm_access.roles del ID token (client scope "roles").
 * Se ignoran los roles internos de Keycloak, como default-roles-donatrack.
 */
public final class RolesKeycloak {

    static final String REGISTRATION_ID = "keycloak";

    private static final Set<String> ROLES = Set.of("ADMIN", "DONANTE", "BENEFICIARIA");

    private RolesKeycloak() {
    }

    public static GrantedAuthoritiesMapper mapper() {
        return authorities -> {
            Set<GrantedAuthority> roles = new LinkedHashSet<>();
            for (GrantedAuthority authority : authorities) {
                Map<String, Object> claims = claimsDe(authority);
                if (claims != null) {
                    roles.addAll(desdeClaims(claims));
                }
            }
            return roles.isEmpty() ? authorities : roles;
        };
    }

    public static Collection<GrantedAuthority> desdeClaims(Map<String, Object> claims) {
        Object acceso = claims.get("realm_access");
        if (!(acceso instanceof Map<?, ?> mapa)) {
            return List.of();
        }
        Object roles = mapa.get("roles");
        if (!(roles instanceof Collection<?> lista)) {
            return List.of();
        }
        Set<GrantedAuthority> autoridades = new LinkedHashSet<>();
        for (Object rol : lista) {
            if (rol instanceof String nombre && ROLES.contains(nombre)) {
                autoridades.add(new SimpleGrantedAuthority("ROLE_" + nombre));
            }
        }
        return autoridades;
    }

    private static Map<String, Object> claimsDe(GrantedAuthority authority) {
        if (authority instanceof OidcUserAuthority oidc) {
            return oidc.getIdToken().getClaims();
        }
        if (authority instanceof OAuth2UserAuthority oauth) {
            return oauth.getAttributes();
        }
        return null;
    }
}
