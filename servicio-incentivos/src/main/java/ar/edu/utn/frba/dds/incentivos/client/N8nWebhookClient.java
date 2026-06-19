package ar.edu.utn.frba.dds.incentivos.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class N8nWebhookClient {

    private final RestClient restClient;

    public N8nWebhookClient(@Value("${n8n.webhook.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public void notificarSolicitudDonacion(int donanteId, int cantItems, int cantTypes, LocalDate fecha) {
        Map<String, Object> payload = Map.of(
                "donante", donanteId,
                "cant-items", cantItems,
                "cant-types", cantTypes,
                "date", fecha.toString()
        );
        try {
            restClient.post()
                    .uri("/webhook/events/solicitud-donacion")
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            System.err.println("[n8n] Error notificando solicitud-donacion: " + e.getMessage());
        }
    }

    public void notificarDonacionEntregada(List<Integer> donanteIds, String beneficiario, LocalDate fecha) {
        Map<String, Object> payload = Map.of(
                "donantes", donanteIds,
                "date", fecha.toString(),
                "beneficiario", beneficiario
        );
        try {
            restClient.post()
                    .uri("/webhook/events/donacion-entregada")
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            System.err.println("[n8n] Error notificando donacion-entregada: " + e.getMessage());
        }
    }

    public void notificarInsigniaOtorgada(String user, String badge, String description) {
        Map<String, Object> payload = Map.of(
                "user", user,
                "badge", badge,
                "description", description
        );
        try {
            restClient.post()
                    .uri("/webhook/events/insignia-otorgada")
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            System.err.println("[n8n] Error notificando insignia-otorgada: " + e.getMessage());
        }
    }
}
