package ar.edu.utn.frba.dds.donaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@io.swagger.v3.oas.annotations.security.SecurityScheme(
        name = "keycloak", type = io.swagger.v3.oas.annotations.enums.SecuritySchemeType.OAUTH2,
        flows = @io.swagger.v3.oas.annotations.security.OAuthFlows(
                authorizationCode = @io.swagger.v3.oas.annotations.security.OAuthFlow(
                        authorizationUrl = "${donatrack.seguridad.keycloak-url}/realms/donatrack/protocol/openid-connect/auth",
                        tokenUrl = "${donatrack.seguridad.keycloak-url}/realms/donatrack/protocol/openid-connect/token",
                        scopes = @io.swagger.v3.oas.annotations.security.OAuthScope(name = "openid", description = "Cuenta de DonaTrack"))))
@io.swagger.v3.oas.annotations.OpenAPIDefinition(
        security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "keycloak", scopes = "openid"))
@EnableScheduling
public class ServicioDonacionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                ServicioDonacionesApplication.class,
                args
        );
    }
}

