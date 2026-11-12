package ar.edu.utn.frba.dds.donaciones.seguridad;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class UsuarioTokenTest {

    private static final List<String> CLIENTES = List.of("servicio-web", "servicio-incentivos");

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void tomaSoloLosRolesDeDonaTrackDelRealm() {
        Jwt jwt = token(Map.of("realm_access", Map.of("roles", List.of("DONANTE", "default-roles-donatrack", "offline_access")),
                "azp", "servicio-web", "preferred_username", "ana@example.org"));

        assertThat(UsuarioToken.autoridades(jwt, CLIENTES)).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_DONANTE");
    }

    @Test
    void soloLaCuentaDeServicioDeUnClienteConfiguradoEsServicio() {
        Jwt servicio = token(Map.of("azp", "servicio-incentivos", "preferred_username", "service-account-servicio-incentivos"));
        Jwt usuarioDelMismoCliente = token(Map.of("azp", "servicio-web", "preferred_username", "ana@example.org"));
        Jwt clienteAjeno = token(Map.of("azp", "otro", "preferred_username", "service-account-otro"));

        assertThat(UsuarioToken.autoridades(servicio, CLIENTES)).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_SERVICIO");
        assertThat(UsuarioToken.autoridades(usuarioDelMismoCliente, CLIENTES)).isEmpty();
        assertThat(UsuarioToken.autoridades(clienteAjeno, CLIENTES)).isEmpty();
    }

    @Test
    void leeDonanteEntidadesYEmailVerificado() {
        var auth = autenticacion(Map.of("donante_id", "12", "entidad_ids", List.of("3", "5"),
                "email", "Ana@Example.org", "email_verified", true));

        assertThat(UsuarioToken.donanteId(auth)).contains(12L);
        assertThat(UsuarioToken.entidadIds(auth)).containsExactly(3L, 5L);
        assertThat(UsuarioToken.emailVerificado(auth)).contains("ana@example.org");
        assertThat(UsuarioToken.emailVerificado(autenticacion(Map.of("email", "ana@example.org", "email_verified", false))))
                .isEmpty();
    }

    @Test
    void unDonanteSoloCargaDonacionesASuNombre() {
        SecurityContextHolder.getContext().setAuthentication(autenticacion(Map.of(
                "realm_access", Map.of("roles", List.of("DONANTE")), "donante_id", "12")));

        assertThatCode(() -> UsuarioToken.exigirDonante(12L)).doesNotThrowAnyException();
        assertThatThrownBy(() -> UsuarioToken.exigirDonante(13L)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void sinTokenNoSeRestringe() {
        assertThatCode(() -> UsuarioToken.exigirDonante(99L)).doesNotThrowAnyException();
    }

    private static Jwt token(Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue("token").header("alg", "none").subject("usuario");
        claims.forEach(builder::claim);
        return builder.build();
    }

    private static JwtAuthenticationToken autenticacion(Map<String, Object> claims) {
        Jwt jwt = token(claims);
        return new JwtAuthenticationToken(jwt, UsuarioToken.autoridades(jwt, CLIENTES));
    }
}
