package ar.edu.utn.frba.dds.web.client.donaciones;

import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Cliente del servicio de Donaciones. Cada persona agrega los métodos que necesitan sus pantallas. */
@Component
public class DonacionesClient {

    private final RestClient http;

    public DonacionesClient(@Qualifier("donaciones") RestClient http) {
        this.http = http;
    }

    /** GET /api/donaciones */
    public List<DonacionApi> donaciones() {
        return http.get().uri("/api/donaciones").retrieve().body(new ParameterizedTypeReference<>() { });
    }

    /** GET /api/entidades */
    public List<EntidadApi> entidades() {
        return http.get().uri("/api/entidades").retrieve().body(new ParameterizedTypeReference<>() { });
    }
}
