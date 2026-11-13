package ar.edu.utn.frba.dds.logistica.seguridad;

import static ar.edu.utn.frba.dds.logistica.seguridad.UsuarioToken.ADMIN;
import static ar.edu.utn.frba.dds.logistica.seguridad.UsuarioToken.BENEFICIARIA;
import static ar.edu.utn.frba.dds.logistica.seguridad.UsuarioToken.SERVICIO;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Parada;
import ar.edu.utn.frba.dds.logistica.repository.RutaRepository;
import ar.edu.utn.frba.dds.logistica.service.ControlDeFotos;
import java.util.List;
import java.util.Objects;
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
 * Con donatrack.seguridad.habilitada=false (default) todo queda abierto:
 * así sigue funcionando el despliegue que usa Donaciones como proveedor alternativo.
 * Quedan públicos el link de seguimiento que se comparte con la entidad y el
 * callback del planificador externo.
 */
@Configuration
public class SeguridadApiConfig {

    private static final Logger log = LoggerFactory.getLogger(SeguridadApiConfig.class);

    @Bean
    SecurityFilterChain filtroApi(HttpSecurity http, RutaRepository rutas, ControlDeFotos controlDeFotos,
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
        AuthorizationManager<RequestAuthorizationContext> paradaDeLaEntidad = (autenticacion, contexto) -> {
            var auth = autenticacion.get();
            if (!UsuarioToken.tieneRol(auth, BENEFICIARIA)) {
                return new AuthorizationDecision(false);
            }
            Optional<Integer> entidad = entidadDeLaParada(rutas, contexto);
            return new AuthorizationDecision(entidad.isPresent()
                    && UsuarioToken.entidadIds(auth).contains(entidad.get().longValue()));
        };

        AuthorizationManager<RequestAuthorizationContext> fotoPublica = (autenticacion, contexto) ->
                new AuthorizationDecision(controlDeFotos.esPublica(contexto.getVariables().get("id")));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/error", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/rutas/{id}/seguimiento").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/planificador/callback").permitAll()
                // Broker de Donaciones (token client_credentials de servicio-donaciones).
                .requestMatchers(HttpMethod.POST, "/api/lotes").hasAnyRole(ADMIN, SERVICIO)
                .requestMatchers(HttpMethod.GET, "/api/eventos").hasAnyRole(ADMIN, SERVICIO)
                .requestMatchers(HttpMethod.POST, "/api/eventos/{id}/marcar-publicado").hasAnyRole(ADMIN, SERVICIO)
                // Landing pública de servicio-web (token de servicio).
                .requestMatchers(HttpMethod.GET, "/api/entregas").hasAnyRole(ADMIN, SERVICIO)
                // La foto se sube antes de confirmar la parada propia. La descarga sigue siendo de administración.
                .requestMatchers(HttpMethod.POST, "/api/fotos").hasAnyRole(ADMIN, BENEFICIARIA)
                // Sin login solo se ve una foto que ya está en una entrega ENTREGADA (galería pública). El resto, administración.
                .requestMatchers(HttpMethod.GET, "/api/fotos/{id}")
                        .access(AuthorizationManagers.anyOf(esAdmin, fotoPublica))
                // Paradas de la entidad del token. El id se vuelve a verificar en el controller.
                .requestMatchers(HttpMethod.GET, "/api/mis-envios").hasAnyRole(ADMIN, BENEFICIARIA)
                // Lo llama Donaciones, después de quedarse solo con las donaciones de ese donante.
                .requestMatchers(HttpMethod.POST, "/api/seguimiento/camiones").hasAnyRole(ADMIN, SERVICIO)
                // La entidad confirma o rechaza la recepción en su parada.
                .requestMatchers(HttpMethod.POST, "/api/rutas/{idRuta}/paradas/{idParada}/confirmar",
                                "/api/rutas/{idRuta}/paradas/{idParada}/recepcion",
                                "/api/rutas/{idRuta}/paradas/{idParada}/no-recibida")
                        .access(AuthorizationManagers.anyOf(esAdmin, paradaDeLaEntidad))
                .anyRequest().hasRole(ADMIN))
            .oauth2ResourceServer(rs -> rs.jwt(jwt -> jwt
                .decoder(decodificador(keycloakUrl, issuer))
                .jwtAuthenticationConverter(UsuarioToken.convertidor(clientesServicio))));
        return http.build();
    }

    private static Optional<Integer> entidadDeLaParada(RutaRepository rutas, RequestAuthorizationContext contexto) {
        try {
            Integer idRuta = Integer.valueOf(contexto.getVariables().get("idRuta"));
            Integer idParada = Integer.valueOf(contexto.getVariables().get("idParada"));
            return rutas.findById(idRuta).flatMap(ruta -> ruta.getParadas().stream()
                    .filter(p -> idParada.equals(p.getIdParada()))
                    .map(Parada::getIdEntidadBeneficiariaAsociada)
                    .filter(Objects::nonNull)
                    .findFirst());
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    static NimbusJwtDecoder decodificador(String keycloakUrl, String issuer) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(keycloakUrl + "/realms/donatrack/protocol/openid-connect/certs").build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
        return decoder;
    }
}
