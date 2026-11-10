package ar.edu.utn.frba.dds.web.client.logistica;

import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Cliente del servicio de Logística. Cada persona agrega los métodos que necesitan sus pantallas. */
@Component
public class LogisticaClient {

    private final RestClient http;

    public LogisticaClient(@Qualifier("logistica") RestClient http) {
        this.http = http;
    }

    /** GET /api/entregas */
    public List<EntregaApi> entregas() {
        return http.get().uri("/api/entregas").retrieve().body(new ParameterizedTypeReference<>() { });
    }
}
