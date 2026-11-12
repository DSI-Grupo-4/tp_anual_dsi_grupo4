package ar.edu.utn.frba.dds.web.client.logistica;

import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Cliente de las entregas para la administración: listado con motivos y reingreso al depósito. */
@Component
public class EntregasClient {

    private final RestClient http;

    public EntregasClient(@Qualifier("logistica") RestClient http) {
        this.http = http;
    }

    /** GET /api/entregas */
    public List<EntregaAdminApi> entregas() {
        return http.get().uri("/api/entregas").retrieve().body(new ParameterizedTypeReference<>() { });
    }

    /**
     * POST /api/entregas/{id}/reingresar: devuelve al depósito una entrega NO_RECIBIDA o FALLIDA (queda REPLANIFICABLE).
     * 409 si la entrega estaba en otro estado.
     */
    public ReingresoApi reingresar(Integer idEntrega) {
        return http.post().uri("/api/entregas/{id}/reingresar", idEntrega).retrieve().body(ReingresoApi.class);
    }
}
