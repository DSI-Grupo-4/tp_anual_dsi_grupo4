package ar.edu.utn.frba.dds.incentivos.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NotificacionesClient {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionesClient.class);
    private final RestClient restClient;

    public NotificacionesClient(@Value("${notificaciones.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public void notificarMisionCompletada(int donanteId, String misionNombre) {
        notificarMisionCompletada(donanteId, misionNombre, null, null);
    }

    public void notificarMisionCompletada(int donanteId, String misionNombre, String medio, String contacto) {
        enviar(
                "Completaste la mision: " + misionNombre + ".",
                medioPreferidoODefault(medio),
                contactoPreferidoODefault(donanteId, contacto)
        );
    }

    public void notificarSubidaCategoria(int donanteId, String nuevaCategoria) {
        notificarSubidaCategoria(donanteId, nuevaCategoria, null, null);
    }

    public void notificarSubidaCategoria(int donanteId, String nuevaCategoria, String medio, String contacto) {
        enviar(
                "Subiste de categoria a: " + nuevaCategoria + ".",
                medioPreferidoODefault(medio),
                contactoPreferidoODefault(donanteId, contacto)
        );
    }

    private void enviar(String mensaje, String medio, String contacto) {
        NotificacionRequest payload = new NotificacionRequest(mensaje, medio, contacto, "incentivos");
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

    private String medioPreferidoODefault(String medio) {
        return medio == null || medio.isBlank() ? "EMAIL" : medio;
    }

    private String contactoPreferidoODefault(int donanteId, String contacto) {
        return contacto == null || contacto.isBlank() ? "donante" + donanteId + "@donatrack.local" : contacto;
    }

    private record NotificacionRequest(String mensaje, String medio, String contacto, String servicioOrigen) {
    }
}
