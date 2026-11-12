package ar.edu.utn.frba.dds.donaciones.seguridad;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Token client_credentials del cliente servicio-donaciones, para las tareas
 * automáticas (envío a Logística, sincronización con Incentivos) y para la
 * API de administración de Keycloak. Se renueva 30 s antes de vencer.
 * Con la seguridad deshabilitada no pide nada y las llamadas salen sin token.
 */
@Component
public class TokenServicio {

    private final boolean habilitada;
    private final String clientId;
    private final String clientSecret;
    private final RestClient keycloak;
    private String token;
    private Instant vence = Instant.EPOCH;

    public TokenServicio(@Value("${donatrack.seguridad.habilitada:false}") boolean habilitada,
                         @Value("${donatrack.seguridad.keycloak-url:http://localhost:8180}") String keycloakUrl,
                         @Value("${donatrack.seguridad.client-id:servicio-donaciones}") String clientId,
                         @Value("${donatrack.seguridad.client-secret:}") String clientSecret) {
        this.habilitada = habilitada;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        var transporte = new SimpleClientHttpRequestFactory();
        transporte.setConnectTimeout(3000);
        transporte.setReadTimeout(5000);
        this.keycloak = RestClient.builder().requestFactory(transporte)
                .baseUrl(keycloakUrl + "/realms/donatrack/protocol/openid-connect").build();
    }

    public boolean habilitada() {
        return habilitada;
    }

    public synchronized Optional<String> obtener() {
        if (!habilitada) {
            return Optional.empty();
        }
        if (token == null || Instant.now().isAfter(vence)) {
            pedir();
        }
        return Optional.of(token);
    }

    /** Agrega el Bearer a cada request. Si Keycloak no responde, falla como un error de red y la tarea reintenta. */
    public ClientHttpRequestInterceptor interceptor() {
        return (request, body, execution) -> {
            obtener().ifPresent(t -> request.getHeaders().setBearerAuth(t));
            return execution.execute(request, body);
        };
    }

    private void pedir() {
        if (clientSecret.isBlank()) {
            throw new ResourceAccessException("Falta KEYCLOAK_CLIENT_SECRET de " + clientId);
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        JsonNode respuesta;
        try {
            respuesta = keycloak.post().uri("/token")
                    .headers(h -> h.setBasicAuth(clientId, clientSecret))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RuntimeException e) {
            throw new ResourceAccessException("No se pudo obtener el token de " + clientId + ": " + e.getMessage());
        }
        if (respuesta == null || !respuesta.hasNonNull("access_token")) {
            throw new ResourceAccessException("Keycloak no devolvió un token para " + clientId);
        }
        token = respuesta.get("access_token").asText();
        vence = Instant.now().plusSeconds(Math.max(0, respuesta.path("expires_in").asLong(60) - 30));
    }
}
