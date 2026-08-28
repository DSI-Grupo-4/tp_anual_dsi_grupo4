package ar.edu.utn.frba.dds.donaciones.client;

import ar.edu.utn.frba.dds.donaciones.dto.DonacionPendienteDTO;
import ar.edu.utn.frba.dds.donaciones.dto.EventoLogisticoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@Component
public class LogisticaClient {

    private static final Logger logger = LoggerFactory.getLogger(LogisticaClient.class);
    private final RestClient restClient;

    public LogisticaClient(@Value("${logistica.base-url:http://localhost:8083}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    // Donaciones hace POST de hasta 100 donaciones "Asignación Realizada" por vez (restricción del enunciado)
    public void enviarLote(List<DonacionPendienteDTO> lote) {
        try {
            restClient.post()
                    .uri("/api/lotes")
                    .body(lote)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            logger.warn("No se pudo enviar el lote a servicio-logistica: {}", e.getMessage());
        }
    }

    // Donaciones consulta esto (pull), Logística nunca la invoca a ella
    public List<EventoLogisticoDTO> obtenerEventosNoPublicados() {
        try {
            return restClient.get()
                    .uri("/api/eventos")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<EventoLogisticoDTO>>() {
                    });
        } catch (Exception e) {
            logger.warn("No se pudieron obtener los eventos de servicio-logistica: {}", e.getMessage());
            return List.of();
        }
    }

    public void marcarPublicado(UUID idEvento) {
        try {
            restClient.post()
                    .uri("/api/eventos/{id}/marcar-publicado", idEvento)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            logger.warn("No se pudo marcar como publicado el evento {} en servicio-logistica: {}", idEvento, e.getMessage());
        }
    }
}
