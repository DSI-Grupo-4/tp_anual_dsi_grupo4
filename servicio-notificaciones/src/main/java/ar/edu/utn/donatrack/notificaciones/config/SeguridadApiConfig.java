package ar.edu.utn.donatrack.notificaciones.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Validación del access token de Keycloak (D-045). Los demás servicios le
 * hablan a Notificaciones por RabbitMQ, que no pasa por acá; la API REST
 * (envío manual y consulta del historial) queda solo para ADMIN.
 * Con donatrack.seguridad.habilitada=false (default) todo queda abierto.
 */
@Configuration
public class SeguridadApiConfig {

    private static final Logger log = LoggerFactory.getLogger(SeguridadApiConfig.class);

    @Bean
    SecurityFilterChain filtroApi(HttpSecurity http,
                                  @Value("${donatrack.seguridad.habilitada:false}") boolean habilitada,
                                  @Value("${donatrack.seguridad.keycloak-url:http://localhost:8180}") String keycloakUrl,
                                  @Value("${donatrack.seguridad.issuer:http://localhost:8180/realms/donatrack}") String issuer)
            throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        if (!habilitada) {
            log.warn("Seguridad de la API deshabilitada (SEGURIDAD_HABILITADA=false): todos los endpoints quedan abiertos.");
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(keycloakUrl + "/realms/donatrack/protocol/openid-connect/certs").build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/error", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()
                .anyRequest().hasRole("ADMIN"))
            .oauth2ResourceServer(rs -> rs.jwt(jwt -> jwt
                .decoder(decoder)
                .jwtAuthenticationConverter(token -> new JwtAuthenticationToken(token, roles(token),
                        token.getClaimAsString("preferred_username")))));
        return http.build();
    }

    static List<GrantedAuthority> roles(Jwt jwt) {
        List<GrantedAuthority> roles = new ArrayList<>();
        Map<String, Object> realm = jwt.getClaimAsMap("realm_access");
        if (realm != null && realm.get("roles") instanceof Collection<?> valores) {
            valores.stream().map(String::valueOf).filter("ADMIN"::equals).distinct()
                    .forEach(rol -> roles.add(new SimpleGrantedAuthority("ROLE_" + rol)));
        }
        return roles;
    }
}
