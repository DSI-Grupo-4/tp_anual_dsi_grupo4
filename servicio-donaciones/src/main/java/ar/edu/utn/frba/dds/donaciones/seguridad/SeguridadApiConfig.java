package ar.edu.utn.frba.dds.donaciones.seguridad;

import static ar.edu.utn.frba.dds.donaciones.seguridad.UsuarioToken.ADMIN;
import static ar.edu.utn.frba.dds.donaciones.seguridad.UsuarioToken.BENEFICIARIA;
import static ar.edu.utn.frba.dds.donaciones.seguridad.UsuarioToken.DONANTE;
import static ar.edu.utn.frba.dds.donaciones.seguridad.UsuarioToken.SERVICIO;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
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
 * Con donatrack.seguridad.habilitada=false (default) todo queda abierto como
 * antes de la Entrega 6, para desarrollo y tests sin Keycloak.
 */
@Configuration
public class SeguridadApiConfig {

    private static final Logger log = LoggerFactory.getLogger(SeguridadApiConfig.class);

    private static final AuthorizationManager<RequestAuthorizationContext> ES_ADMIN = AuthorityAuthorizationManager.hasRole(ADMIN);
    private static final AuthorizationManager<RequestAuthorizationContext> ES_SERVICIO = AuthorityAuthorizationManager.hasRole(SERVICIO);

    @Bean
    SecurityFilterChain filtroApi(HttpSecurity http, PropiedadRecursos propiedad,
                                  @Value("${donatrack.seguridad.habilitada:false}") boolean habilitada,
                                  @Value("${donatrack.seguridad.keycloak-url:http://localhost:8180}") String keycloakUrl,
                                  @Value("${donatrack.seguridad.issuer:http://localhost:8180/realms/donatrack}") String issuer,
                                  @Value("${donatrack.seguridad.clientes-servicio:servicio-web,servicio-incentivos}") List<String> clientesServicio)
            throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        if (!habilitada) {
            log.warn("Seguridad de la API deshabilitada (SEGURIDAD_HABILITADA=false): todos los endpoints quedan abiertos.");
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        var donantePropio = delDonante(id -> Optional.of(id));
        var entidadPropia = deLaEntidad(id -> Optional.of(id));
        var donacionDelDonante = delDonante(propiedad::donanteDeDonacion);
        var donacionDeLaEntidad = deLaEntidad(propiedad::entidadDeDonacion);
        var solicitudDelDonante = delDonante(propiedad::donanteDeSolicitud);

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/error", "/ping", "/actuator/health", "/actuator/health/**",
                        "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**",
                        "/api/publico/**").permitAll()
                .requestMatchers("/api/administracion/**").hasRole(ADMIN)
                .requestMatchers(HttpMethod.POST, "/api/cuentas/vincular").authenticated()

                .requestMatchers(HttpMethod.GET, "/api/donantes").hasAnyRole(ADMIN, SERVICIO)
                .requestMatchers(HttpMethod.GET, "/api/donantes/{id}").access(AuthorizationManagers.anyOf(ES_ADMIN, ES_SERVICIO, donantePropio))
                .requestMatchers(HttpMethod.PUT, "/api/donantes/{id}").access(AuthorizationManagers.anyOf(ES_ADMIN, donantePropio))
                .requestMatchers(HttpMethod.POST, "/api/donantes/{id}/interacciones").access(AuthorizationManagers.anyOf(ES_ADMIN, donantePropio))
                .requestMatchers("/api/donantes/**").hasRole(ADMIN)

                .requestMatchers(HttpMethod.GET, "/api/entidades").hasAnyRole(ADMIN, SERVICIO)
                .requestMatchers(HttpMethod.GET, "/api/entidades/{id}", "/api/entidades/{id}/necesidades")
                        .access(AuthorizationManagers.anyOf(ES_ADMIN, ES_SERVICIO, entidadPropia))
                .requestMatchers(HttpMethod.PUT, "/api/entidades/{id}").access(AuthorizationManagers.anyOf(ES_ADMIN, entidadPropia))
                .requestMatchers("/api/entidades/{id}/necesidades", "/api/entidades/{id}/necesidades/**")
                        .access(AuthorizationManagers.anyOf(ES_ADMIN, entidadPropia))
                .requestMatchers("/api/entidades/**").hasRole(ADMIN)

                // El donante del body se controla en DonacionController (UsuarioToken.exigirDonante).
                .requestMatchers(HttpMethod.POST, "/api/donaciones").hasAnyRole(ADMIN, DONANTE)
                .requestMatchers(HttpMethod.GET, "/api/donaciones", "/api/donaciones/pendientes").hasAnyRole(ADMIN, SERVICIO)
                .requestMatchers(HttpMethod.GET, "/api/donaciones/{id}", "/api/donaciones/{id}/historial")
                        .access(AuthorizationManagers.anyOf(ES_ADMIN, ES_SERVICIO, donacionDelDonante, donacionDeLaEntidad))
                .requestMatchers(HttpMethod.PUT, "/api/donaciones/{id}").access(AuthorizationManagers.anyOf(ES_ADMIN, donacionDelDonante))
                .requestMatchers(HttpMethod.DELETE, "/api/donaciones/{id}").access(AuthorizationManagers.anyOf(ES_ADMIN, donacionDelDonante))
                .requestMatchers("/api/donaciones/**").hasRole(ADMIN)
                .requestMatchers(HttpMethod.DELETE, "/api/solicitudes/{id}").access(AuthorizationManagers.anyOf(ES_ADMIN, solicitudDelDonante))

                // Matchmaking manual, envío y consumo de Logística, sincronización de cuentas.
                .anyRequest().hasRole(ADMIN))
            .oauth2ResourceServer(rs -> rs.jwt(jwt -> jwt
                .decoder(decodificador(keycloakUrl, issuer))
                .jwtAuthenticationConverter(UsuarioToken.convertidor(clientesServicio))));
        return http.build();
    }

    /** Las claves se bajan de Keycloak recién con el primer token: el servicio arranca aunque Keycloak esté apagado. */
    static NimbusJwtDecoder decodificador(String keycloakUrl, String issuer) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(keycloakUrl + "/realms/donatrack/protocol/openid-connect/certs").build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }

    /** DONANTE cuyo donante_id coincide con el dueño del recurso de la URL ({id}). */
    private static AuthorizationManager<RequestAuthorizationContext> delDonante(Function<Long, Optional<Long>> dueno) {
        return (autenticacion, contexto) -> {
            var auth = autenticacion.get();
            if (!UsuarioToken.tieneRol(auth, DONANTE)) {
                return new AuthorizationDecision(false);
            }
            Optional<Long> propio = UsuarioToken.donanteId(auth);
            Optional<Long> recurso = idDeLaUrl(contexto).flatMap(dueno);
            return new AuthorizationDecision(propio.isPresent() && propio.equals(recurso));
        };
    }

    /** BENEFICIARIA que representa a la entidad dueña del recurso de la URL ({id}). */
    private static AuthorizationManager<RequestAuthorizationContext> deLaEntidad(Function<Long, Optional<Long>> dueno) {
        return (autenticacion, contexto) -> {
            var auth = autenticacion.get();
            if (!UsuarioToken.tieneRol(auth, BENEFICIARIA)) {
                return new AuthorizationDecision(false);
            }
            Optional<Long> recurso = idDeLaUrl(contexto).flatMap(dueno);
            return new AuthorizationDecision(recurso.isPresent() && UsuarioToken.entidadIds(auth).contains(recurso.get()));
        };
    }

    private static Optional<Long> idDeLaUrl(RequestAuthorizationContext contexto) {
        try {
            return Optional.of(Long.parseLong(contexto.getVariables().get("id")));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
