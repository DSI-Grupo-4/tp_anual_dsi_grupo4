package ar.edu.utn.frba.dds.web.client.logistica;

import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Cliente de los choferes de Logística (ABM de la administración). */
@Component
public class ChoferesClient {

    private final RestClient http;

    public ChoferesClient(@Qualifier("logistica") RestClient http) {
        this.http = http;
    }

    /** GET /api/choferes */
    public List<ChoferApi> choferes() {
        return http.get().uri("/api/choferes").retrieve().body(new ParameterizedTypeReference<>() { });
    }

    /** POST /api/choferes */
    public ChoferApi crear(ChoferRequest pedido) {
        return http.post().uri("/api/choferes").body(pedido).retrieve().body(ChoferApi.class);
    }

    /** PUT /api/choferes/{id} */
    public ChoferApi actualizar(Integer id, ChoferRequest pedido) {
        return http.put().uri("/api/choferes/{id}", id).body(pedido).retrieve().body(ChoferApi.class);
    }

    /** DELETE /api/choferes/{id} */
    public void eliminar(Integer id) {
        http.delete().uri("/api/choferes/{id}", id).retrieve().toBodilessEntity();
    }
}
