package ar.edu.utn.frba.dds.web.client.donaciones;

import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente de las necesidades de una entidad beneficiaria y de su ficha (portal beneficiario).
 * Está separado de DonacionesClient para que las pantallas de cada persona no se pisen al integrar.
 */
@Component
public class NecesidadesClient {

    private final RestClient http;

    public NecesidadesClient(@Qualifier("donaciones") RestClient http) {
        this.http = http;
    }

    /** GET /api/entidades/{id} */
    public EntidadApi entidad(Long entidadId) {
        return http.get().uri("/api/entidades/{id}", entidadId).retrieve().body(EntidadApi.class);
    }

    /** GET /api/entidades/{id}/necesidades */
    public List<NecesidadApi> necesidades(Long entidadId) {
        return http.get().uri("/api/entidades/{id}/necesidades", entidadId)
                .retrieve().body(new ParameterizedTypeReference<>() { });
    }

    /** POST /api/entidades/{id}/necesidades/recurrentes o /extraordinarias, según el tipo del pedido. */
    public NecesidadApi crear(Long entidadId, NecesidadRequest pedido) {
        String ruta = "RECURRENTE".equalsIgnoreCase(pedido.tipo()) ? "recurrentes" : "extraordinarias";
        return http.post().uri("/api/entidades/{id}/necesidades/" + ruta, entidadId)
                .body(pedido).retrieve().body(NecesidadApi.class);
    }

    /** PUT /api/entidades/{id}/necesidades/{necesidadId} */
    public NecesidadApi actualizar(Long entidadId, Long necesidadId, NecesidadRequest pedido) {
        return http.put().uri("/api/entidades/{id}/necesidades/{n}", entidadId, necesidadId)
                .body(pedido).retrieve().body(NecesidadApi.class);
    }

    /** DELETE /api/entidades/{id}/necesidades/{necesidadId} */
    public void eliminar(Long entidadId, Long necesidadId) {
        http.delete().uri("/api/entidades/{id}/necesidades/{n}", entidadId, necesidadId)
                .retrieve().toBodilessEntity();
    }
}
