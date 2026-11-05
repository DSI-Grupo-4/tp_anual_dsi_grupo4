package ar.edu.utn.frba.dds.donaciones.client;

import ar.edu.utn.frba.dds.donaciones.dto.DonacionPendienteDTO;
import ar.edu.utn.frba.dds.donaciones.dto.EventoLogisticoDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

public class LogisticaClient {

    // El proveedor ALTERNATIVA corre en el free tier de Render, que suspende
    // la instancia tras inactividad: el connect es casi siempre rápido (lo
    // acepta el edge de Render/Cloudflare), pero la primera respuesta puede
    // demorar bastante mientras la instancia despierta. Sin un read timeout
    // explícito, esa demora cuelga el único hilo del scheduler muchísimo más
    // que el intervalo de polling, dando la falsa sensación de que no hay
    // conexión.
    private static final int CONNECT_TIMEOUT_MS = 5_000;
    private static final int READ_TIMEOUT_MS = 20_000;

    private final String nombre;
    private final RestClient restClient;

    public LogisticaClient(String nombre, String baseUrl) {
        this.nombre = nombre;
        var transporte = new SimpleClientHttpRequestFactory();
        transporte.setConnectTimeout(CONNECT_TIMEOUT_MS);
        transporte.setReadTimeout(READ_TIMEOUT_MS);
        this.restClient = RestClient.builder().requestFactory(transporte).baseUrl(baseUrl).build();
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
