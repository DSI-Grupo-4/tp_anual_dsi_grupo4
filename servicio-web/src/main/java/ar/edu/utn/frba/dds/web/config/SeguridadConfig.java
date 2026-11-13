package ar.edu.utn.frba.dds.web.config;

import ar.edu.utn.frba.dds.web.mensajes.Toast;
import ar.edu.utn.frba.dds.web.mensajes.Toasts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.SessionFlashMapManager;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Cliente OIDC confidencial contra el realm donatrack.
 * Los endpoints se declaran a mano, sin issuer-uri, para no consultar a Keycloak
 * al arrancar: el cliente web tiene que levantar aunque el perfil auth esté apagado.
 * El logout redirige al end_session de Keycloak para cerrar también la sesión SSO.
 *
 * El mismo cliente tiene un segundo registro con client_credentials: es el token
 * con el que la landing pública consulta las APIs sin un usuario logueado.
 */
@Configuration
public class SeguridadConfig {

    static final String REGISTRATION_SERVICIO = "keycloak-servicio";
    static final String COOKIE_SESION = "DONATRACK_SESSION";

    private static final Logger log = LoggerFactory.getLogger(SeguridadConfig.class);

    @Bean
    ClientRegistrationRepository registrosKeycloak(
            @Value("${donatrack.web.auth.keycloak-url}") String keycloakUrl,
            @Value("${donatrack.web.auth.client-secret:}") String clientSecret) {
        if (clientSecret.isBlank()) {
            log.warn("Falta KEYCLOAK_CLIENT_SECRET: el login va a llegar a Keycloak y fallar al canjear el código.");
        }
        String secret = clientSecret.isBlank() ? "definir-en-el-entorno" : clientSecret;
        String base = sinBarraFinal(keycloakUrl) + "/realms/donatrack/protocol/openid-connect";
        ClientRegistration login = ClientRegistration.withRegistrationId(RolesKeycloak.REGISTRATION_ID)
                .clientId("servicio-web")
                .clientSecret(secret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .authorizationUri(base + "/auth")
                .tokenUri(base + "/token")
                .jwkSetUri(base + "/certs")
                .userInfoUri(base + "/userinfo")
                .userNameAttributeName("preferred_username")
                .clientName("servicio-web")
                .providerConfigurationMetadata(Map.of("end_session_endpoint", base + "/logout"))
                .build();
        ClientRegistration servicio = ClientRegistration.withRegistrationId(REGISTRATION_SERVICIO)
                .clientId("servicio-web")
                .clientSecret(secret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .tokenUri(base + "/token")
                .build();
        return new InMemoryClientRegistrationRepository(login, servicio);
    }

    /** Los tokens del usuario viven en su sesión HTTP: se pierden con el logout o cuando la sesión vence. */
    @Bean
    OAuth2AuthorizedClientRepository clientesAutorizados() {
        return new HttpSessionOAuth2AuthorizedClientRepository();
    }

    @Bean
    OAuth2AuthorizedClientService clientesAutorizadosServicio(ClientRegistrationRepository registros) {
        return new InMemoryOAuth2AuthorizedClientService(registros);
    }

    /** Token del usuario logueado; con el refresh token lo renueva cuando vence el access token. */
    @Bean
    OAuth2AuthorizedClientManager tokensUsuario(ClientRegistrationRepository registros,
                                                OAuth2AuthorizedClientRepository clientes) {
        var manager = new DefaultOAuth2AuthorizedClientManager(registros, clientes);
        manager.setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder()
                .authorizationCode()
                .refreshToken()
                .build());
        return manager;
    }

    /** Token client_credentials de servicio-web; no es un bean para no competir con el del usuario. */
    static OAuth2AuthorizedClientManager tokenServicio(ClientRegistrationRepository registros,
                                                       OAuth2AuthorizedClientService clientes) {
        var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(registros, clientes);
        manager.setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build());
        return manager;
    }

    @Bean
    SecurityFilterChain filtroWeb(HttpSecurity http, ClientRegistrationRepository registros) throws Exception {
        OidcClientInitiatedLogoutSuccessHandler cierre = new OidcClientInitiatedLogoutSuccessHandler(registros);
        cierre.setPostLogoutRedirectUri("{baseUrl}");
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin", "/admin/", "/admin/**").hasRole("ADMIN")
                        .requestMatchers("/donante", "/donante/", "/donante/**").hasRole("DONANTE")
                        .requestMatchers("/beneficiaria", "/beneficiaria/", "/beneficiaria/**").hasRole("BENEFICIARIA")
                        .requestMatchers("/cuenta", "/cuenta/**").authenticated()
                        .anyRequest().permitAll())
                .oauth2Login(login -> login
                        .loginPage("/oauth2/authorization/keycloak")
                        .authorizationEndpoint(a -> a.authorizationRequestResolver(conRegistro(registros)))
                        .userInfoEndpoint(info -> info.userAuthoritiesMapper(RolesKeycloak.mapper()))
                        .successHandler(new AlIngresar()))
                .sessionManagement(sesion -> sesion.invalidSessionStrategy(SeguridadConfig::sesionVencida))
                .logout(logout -> logout
                        .logoutSuccessHandler(cierre)
                        .deleteCookies(COOKIE_SESION));
        return http.build();
    }

    /**
     * /oauth2/authorization/keycloak?registro abre directamente el formulario de
     * registro de Keycloak (prompt=create) en vez del de login.
     */
    static OAuth2AuthorizationRequestResolver conRegistro(ClientRegistrationRepository registros) {
        var base = new DefaultOAuth2AuthorizationRequestResolver(registros,
                OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);
        return new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                return conPrompt(request, base.resolve(request));
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String registrationId) {
                return conPrompt(request, base.resolve(request, registrationId));
            }
        };
    }

    private static OAuth2AuthorizationRequest conPrompt(HttpServletRequest request, OAuth2AuthorizationRequest pedido) {
        if (pedido == null || request.getParameter("registro") == null) {
            return pedido;
        }
        // from() copia la URI ya armada: el parámetro nuevo se agrega también a ella.
        String uri = UriComponentsBuilder.fromUriString(pedido.getAuthorizationRequestUri())
                .queryParam("prompt", "create").build(true).toUriString();
        return OAuth2AuthorizationRequest.from(pedido)
                .additionalParameters(p -> p.put("prompt", "create"))
                .authorizationRequestUri(uri)
                .build();
    }

    /**
     * La cookie apunta a una sesión que ya no existe. En las zonas privadas se avisa
     * antes de que el login vuelva a pedir credenciales; en las públicas solo se
     * repite el pedido con una sesión nueva.
     */
    private static void sesionVencida(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.getSession(true);
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        if (ruta.startsWith("/admin") || ruta.startsWith("/donante") || ruta.startsWith("/beneficiaria") || ruta.startsWith("/cuenta")) {
            avisar(request, response, Toast.Tipo.INFO, "Tu sesión venció. Volvé a ingresar para seguir.");
        }
        response.sendRedirect("GET".equals(request.getMethod()) ? request.getRequestURI() : request.getContextPath() + "/");
    }

    /** Toast para la próxima página, con el mismo atributo flash que usa {@link Toasts}. */
    static void avisar(HttpServletRequest request, HttpServletResponse response, Toast.Tipo tipo, String texto) {
        FlashMap flash = new FlashMap();
        List<Toast> toasts = new ArrayList<>();
        toasts.add(new Toast(tipo, texto));
        flash.put(Toasts.ATRIBUTO, toasts);
        new SessionFlashMapManager().saveOutputFlashMap(flash, request, response);
    }

    /** Una cuenta sin rol de DonaTrack todavía no está vinculada a un donante o entidad: va a /cuenta. */
    static final class AlIngresar extends SavedRequestAwareAuthenticationSuccessHandler {
        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                            Authentication authentication) throws IOException, jakarta.servlet.ServletException {
            boolean conRol = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().startsWith("ROLE_"));
            if (!conRol) {
                getRedirectStrategy().sendRedirect(request, response, "/cuenta");
                return;
            }
            super.onAuthenticationSuccess(request, response, authentication);
        }

        @Override
        protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
                                            Authentication authentication) {
            for (String rol : List.of("ADMIN", "DONANTE", "BENEFICIARIA")) {
                if (authentication.getAuthorities().stream().anyMatch(a -> ("ROLE_" + rol).equals(a.getAuthority()))) {
                    return switch (rol) {
                        case "ADMIN" -> "/admin";
                        case "DONANTE" -> "/donante";
                        default -> "/beneficiaria";
                    };
                }
            }
            return "/cuenta";
        }
    }

    private static String sinBarraFinal(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
