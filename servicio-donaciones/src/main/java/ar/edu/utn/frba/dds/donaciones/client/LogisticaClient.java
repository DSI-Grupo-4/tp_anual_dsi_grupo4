package ar.edu.utn.frba.dds.donaciones.client;

import ar.edu.utn.frba.dds.donaciones.dto.DonacionPendienteDTO;
import ar.edu.utn.frba.dds.donaciones.dto.EventoLogisticoDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

public class LogisticaClient {

    private final String nombre;
    private final RestClient restClient;

    public LogisticaClient(String nombre, String baseUrl) {
        this.nombre = nombre;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public String getNombre() {
        return nombre;
    }

    public void enviarLote(List<DonacionPendienteDTO> lote) {
        restClient.post()
                .uri("/api/lotes")
                .body(lote)
                .retrieve()
                .toBodilessEntity();
    }

    public List<EventoLogisticoDTO> obtenerEventosNoPublicados() {
        List<EventoLogisticoDTO> eventos = restClient.get()
                .uri("/api/eventos")
                .retrieve()
                .body(new ParameterizedTypeReference<List<EventoLogisticoDTO>>() {
                });
        return eventos == null ? List.of() : eventos;
    }

    public void marcarPublicado(UUID idEvento) {
        restClient.post()
                .uri("/api/eventos/{id}/marcar-publicado", idEvento)
                .retrieve()
                .toBodilessEntity();
    }
}
