package ar.edu.utn.frba.dds.incentivos.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class NotificacionesClient {

    private final RestClient restClient;

    public NotificacionesClient(@Value("${notificaciones.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public void notificarMisionCompletada(int donanteId, String misionNombre) {
        Map<String, Object> payload = Map.of(
                "donanteId", donanteId,
                "mensaje", "¡Completaste la misión: " + misionNombre + "!"
        );
        try {
            restClient.post()
                    .uri("/api/notificaciones/enviar")
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            System.err.println("[notificaciones] Error: " + e.getMessage());
        }
    }

    public void notificarSubidaCategoria(int donanteId, String nuevaCategoria) {
        Map<String, Object> payload = Map.of(
                "donanteId", donanteId,
                "mensaje", "¡Subiste de categoría a: " + nuevaCategoria + "!"
        );
        try {
            restClient.post()
                    .uri("/api/notificaciones/enviar")
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            System.err.println("[notificaciones] Error: " + e.getMessage());
        }
    }
}
