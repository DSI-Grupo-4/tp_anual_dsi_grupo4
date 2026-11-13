package ar.edu.utn.frba.dds.web.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;

class RolesKeycloakTest {

    @Test
    void tomaSoloLosTresRolesDelRealm() {
        Map<String, Object> claims = Map.of(
                "realm_access", Map.of("roles", List.of("ADMIN", "default-roles-donatrack", "uma_authorization")));

        assertThat(RolesKeycloak.desdeClaims(claims))
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void sinClaimDeRolesNoInventaAutoridades() {
        assertThat(RolesKeycloak.desdeClaims(Map.of("sub", "1"))).isEmpty();
    }

    @Test
    void elMapperLeeLosRolesDelIdToken() {
        OidcIdToken token = OidcIdToken.withTokenValue("token-de-prueba")
                .claim("sub", "1")
                .claim("realm_access", Map.of("roles", List.of("DONANTE")))
                .issuedAt(Instant.parse("2026-11-11T00:00:00Z"))
                .expiresAt(Instant.parse("2026-11-11T01:00:00Z"))
                .build();

        assertThat(RolesKeycloak.mapper().mapAuthorities(List.of(
                        new OidcUserAuthority(token),
                        new SimpleGrantedAuthority("SCOPE_openid"))))
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_DONANTE");
    }
}
