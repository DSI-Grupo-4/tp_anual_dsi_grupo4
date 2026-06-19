package ar.edu.utn.frba.dds.donaciones.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NotificacionesClient {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionesClient.class);
    private final RestClient restClient;

    public NotificacionesClient(@Value("${notificaciones.base-url:http://localhost:8083}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public void enviar(String mensaje, String medio, String contacto) {
        NotificacionRequest payload = new NotificacionRequest(mensaje, medio, contacto, "donaciones");
        try {
            restClient.post()
                    .uri("/api/notificaciones")
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            logger.warn("No se pudo solicitar notificacion al servicio de notificaciones: {}", e.getMessage());
        }
    }

    private record NotificacionRequest(String mensaje, String medio, String contacto, String servicioOrigen) {
    }
}
