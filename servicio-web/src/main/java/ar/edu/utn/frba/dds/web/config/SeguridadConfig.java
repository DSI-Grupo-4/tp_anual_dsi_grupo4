package ar.edu.utn.frba.dds.web.config;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cliente OIDC confidencial contra el realm donatrack.
 * Los endpoints se declaran a mano, sin issuer-uri, para no consultar a Keycloak
 * al arrancar: el cliente web tiene que levantar aunque el perfil auth esté apagado.
 * El logout redirige al end_session de Keycloak para cerrar también la sesión SSO.
 */
@Configuration
public class SeguridadConfig {

    private static final Logger log = LoggerFactory.getLogger(SeguridadConfig.class);

    @Bean
    ClientRegistrationRepository registrosKeycloak(
            @Value("${donatrack.web.auth.keycloak-url}") String keycloakUrl,
            @Value("${donatrack.web.auth.client-secret:}") String clientSecret) {
        if (clientSecret.isBlank()) {
            log.warn("Falta KEYCLOAK_CLIENT_SECRET: el login va a llegar a Keycloak y fallar al canjear el código.");
        }
        String base = sinBarraFinal(keycloakUrl) + "/realms/donatrack/protocol/openid-connect";
        ClientRegistration registro = ClientRegistration.withRegistrationId(RolesKeycloak.REGISTRATION_ID)
                .clientId("servicio-web")
                .clientSecret(clientSecret.isBlank() ? "definir-en-el-entorno" : clientSecret)
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
        return new InMemoryClientRegistrationRepository(registro);
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
                        .anyRequest().permitAll())
                .oauth2Login(login -> login
                        .loginPage("/oauth2/authorization/keycloak")
                        .userInfoEndpoint(info -> info.userAuthoritiesMapper(RolesKeycloak.mapper())))
                .logout(logout -> logout.logoutSuccessHandler(cierre));
        return http.build();
    }

    private static String sinBarraFinal(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
