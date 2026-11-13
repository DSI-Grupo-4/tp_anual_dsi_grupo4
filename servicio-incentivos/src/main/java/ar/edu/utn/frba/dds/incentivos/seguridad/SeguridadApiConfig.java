package ar.edu.utn.frba.dds.incentivos.seguridad;

import static ar.edu.utn.frba.dds.incentivos.seguridad.UsuarioToken.ADMIN;
import static ar.edu.utn.frba.dds.incentivos.seguridad.UsuarioToken.DONANTE;
import static ar.edu.utn.frba.dds.incentivos.seguridad.UsuarioToken.SERVICIO;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

/**
 * Validación del access token de Keycloak y reglas por endpoint (D-045).
 * Con donatrack.seguridad.habilitada=false (default) todo queda abierto.
 * El ranking sigue público: lo consulta el flujo programado de n8n sin token.
 */
@Configuration
public class SeguridadApiConfig {

    private static final Logger log = LoggerFactory.getLogger(SeguridadApiConfig.class);

    @Bean
    SecurityFilterChain filtroApi(HttpSecurity http,
                                  @Value("${donatrack.seguridad.habilitada:false}") boolean habilitada,
                                  @Value("${donatrack.seguridad.keycloak-url:http://localhost:8180}") String keycloakUrl,
                                  @Value("${donatrack.seguridad.issuer:http://localhost:8180/realms/donatrack}") String issuer,
                                  @Value("${donatrack.seguridad.clientes-servicio:servicio-donaciones,servicio-web}") List<String> clientesServicio)
            throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        if (!habilitada) {
            log.warn("Seguridad de la API deshabilitada (SEGURIDAD_HABILITADA=false): todos los endpoints quedan abiertos.");
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        AuthorizationManager<RequestAuthorizationContext> esAdmin = AuthorityAuthorizationManager.hasRole(ADMIN);
        AuthorizationManager<RequestAuthorizationContext> donantePropio = (autenticacion, contexto) -> {
            var auth = autenticacion.get();
            Optional<Long> propio = UsuarioToken.donanteId(auth);
            return new AuthorizationDecision(UsuarioToken.tieneRol(auth, DONANTE)
                    && propio.isPresent() && String.valueOf(propio.get()).equals(contexto.getVariables().get("id")));
        };

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/error", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/ranking/**").permitAll()
                // Sincronización desde Donaciones (IncentivosClient con token client_credentials).
                .requestMatchers(HttpMethod.PUT, "/api/donantes/{id}/perfil").hasRole(SERVICIO)
                .requestMatchers(HttpMethod.POST, "/api/donantes/{id}/actividad-donacion").hasRole(SERVICIO)
                .requestMatchers(HttpMethod.GET, "/api/donantes/{id}/metricas", "/api/donantes/{id}/misiones", "/api/donantes/{id}/insignias",
                        "/api/donantes/{id}/insignias/obtenidas")
                        .access(AuthorizationManagers.anyOf(esAdmin, donantePropio))
                .requestMatchers(HttpMethod.PATCH, "/api/donantes/{id}/insignias/**")
                        .access(AuthorizationManagers.anyOf(esAdmin, donantePropio))
                .anyRequest().hasRole(ADMIN))
            .oauth2ResourceServer(rs -> rs.jwt(jwt -> jwt
                .decoder(decodificador(keycloakUrl, issuer))
                .jwtAuthenticationConverter(UsuarioToken.convertidor(clientesServicio))));
        return http.build();
    }

    static NimbusJwtDecoder decodificador(String keycloakUrl, String issuer) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(keycloakUrl + "/realms/donatrack/protocol/openid-connect/certs").build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }
}
