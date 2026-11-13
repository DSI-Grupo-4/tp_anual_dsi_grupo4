package ar.edu.utn.frba.dds.donaciones.client;

import ar.edu.utn.frba.dds.donaciones.dto.CamionEnCursoDTO;
import ar.edu.utn.frba.dds.donaciones.dto.DonacionPendienteDTO;
import ar.edu.utn.frba.dds.donaciones.dto.EventoLogisticoDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LogisticaClient {

    private final String nombre;
    private final RestClient restClient;

    public LogisticaClient(String nombre, String baseUrl) {
        this(nombre, baseUrl, null);
    }

    /** credenciales: token de servicio; null para proveedores externos, que no deben recibirlo. */
    public LogisticaClient(String nombre, String baseUrl, ClientHttpRequestInterceptor credenciales) {
        this.nombre = nombre;
        RestClient.Builder builder = RestClient.builder().baseUrl(baseUrl);
        if (credenciales != null) {
            builder.requestInterceptor(credenciales);
        }
        this.restClient = builder.build();
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

    /** POST /api/seguimiento/camiones. Solo el proveedor propio entiende este pedido. */
    public List<CamionEnCursoDTO> camiones(List<Integer> idsDonacion) {
        List<CamionEnCursoDTO> camiones = restClient.post()
                .uri("/api/seguimiento/camiones")
                .body(Map.of("idsDonacion", idsDonacion))
                .retrieve()
                .body(new ParameterizedTypeReference<List<CamionEnCursoDTO>>() {
                });
        return camiones == null ? List.of() : camiones;
    }

    public void marcarPublicado(UUID idEvento) {
        restClient.post()
                .uri("/api/eventos/{id}/marcar-publicado", idEvento)
                .retrieve()
                .toBodilessEntity();
    }
}
