package ar.edu.utn.frba.dds.web.client.logistica;

import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Cliente de los camiones de Logística (ABM de la administración). */
@Component
public class CamionesClient {

    private final RestClient http;

    public CamionesClient(@Qualifier("logistica") RestClient http) {
        this.http = http;
    }

    /** GET /api/camiones */
    public List<CamionApi> camiones() {
        return http.get().uri("/api/camiones").retrieve().body(new ParameterizedTypeReference<>() { });
    }

    /** POST /api/camiones */
    public CamionApi crear(CamionRequest pedido) {
        return http.post().uri("/api/camiones").body(pedido).retrieve().body(CamionApi.class);
    }

    /** PUT /api/camiones/{id} */
    public CamionApi actualizar(Integer id, CamionRequest pedido) {
        return http.put().uri("/api/camiones/{id}", id).body(pedido).retrieve().body(CamionApi.class);
    }

    /** DELETE /api/camiones/{id} */
    public void eliminar(Integer id) {
        http.delete().uri("/api/camiones/{id}", id).retrieve().toBodilessEntity();
    }
}
