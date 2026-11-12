package ar.edu.utn.frba.dds.web.client;

import ar.edu.utn.frba.dds.web.error.AccesoDenegadoException;
import ar.edu.utn.frba.dds.web.error.BackendException;
import ar.edu.utn.frba.dds.web.error.BackendNoDisponibleException;
import ar.edu.utn.frba.dds.web.error.BackendRechazoException;
import ar.edu.utn.frba.dds.web.error.RecursoNoEncontradoException;
import ar.edu.utn.frba.dds.web.error.ServicioBackend;
import ar.edu.utn.frba.dds.web.error.SesionVencidaException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;

/**
 * Configura un RestClient.Builder con el comportamiento común a todos los clientes:
 * credenciales (Persona 2) y traducción de errores HTTP o de red a {@link BackendException}.
 * Así los controllers nunca ven excepciones de HTTP crudas.
 */
public final class ManejoErroresApi {

    private static final ObjectMapper JSON = new ObjectMapper();

    private ManejoErroresApi() {
    }

    public static RestClient.Builder configurar(RestClient.Builder builder, ServicioBackend servicio,
                                                PropagadorCredenciales credenciales) {
        return builder
                .requestInterceptor((request, body, execution) -> {
                    credenciales.aplicar(request.getHeaders());
                    try {
                        return execution.execute(request, body);
                    } catch (IOException e) {
                        // Conexión rechazada, timeout de conexión o de lectura.
                        throw new BackendNoDisponibleException(servicio, 0, e);
                    }
                })
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    throw traducir(servicio, response);
                });
    }

    static BackendException traducir(ServicioBackend servicio, ClientHttpResponse response) throws IOException {
        int status = response.getStatusCode().value();
        JsonNode cuerpo = leer(response);
        String mensaje = cuerpo.path("error").asText("");
        if (mensaje.isBlank()) {
            mensaje = "El servicio de " + servicio.getNombreVisible() + " respondió con error " + status;
        }
        return switch (status) {
            case 401 -> new SesionVencidaException(servicio, mensaje, null);
            case 403 -> new AccesoDenegadoException(servicio, mensaje);
            case 404 -> new RecursoNoEncontradoException(servicio, mensaje);
            case 502, 503, 504 -> new BackendNoDisponibleException(servicio, status, null);
            case 400, 409, 413, 422 -> new BackendRechazoException(servicio, status, mensaje, detalles(cuerpo));
            default -> new BackendException(servicio, status, mensaje, null);
        };
    }

    private static JsonNode leer(ClientHttpResponse response) {
        try (InputStream in = response.getBody()) {
            JsonNode nodo = JSON.readTree(in);
            return nodo == null ? JSON.createObjectNode() : nodo;
        } catch (IOException | RuntimeException e) {
            return JSON.createObjectNode(); // cuerpo vacío o no JSON
        }
    }

    private static Map<String, String> detalles(JsonNode cuerpo) {
        Map<String, String> errores = new LinkedHashMap<>();
        cuerpo.path("detalles").fields().forEachRemaining(e -> errores.put(e.getKey(), e.getValue().asText()));
        return errores;
    }
}
