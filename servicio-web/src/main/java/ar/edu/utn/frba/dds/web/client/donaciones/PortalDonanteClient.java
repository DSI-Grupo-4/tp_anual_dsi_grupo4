package ar.edu.utn.frba.dds.web.client.donaciones;

import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.DonacionDelDonante;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Pagina;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Perfil;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Lecturas del servicio de Donaciones para el portal del donante (Persona 4).
 * Separado de DonacionesClient y AdminDonacionesClient para no pisar el trabajo de otras personas.
 */
@Component
public class PortalDonanteClient {

    private final RestClient http;

    public PortalDonanteClient(@Qualifier("donaciones") RestClient http) {
        this.http = http;
    }

    /** GET /api/donantes/{id} */
    public Perfil perfil(long donanteId) {
        return http.get().uri("/api/donantes/{id}", donanteId).retrieve().body(Perfil.class);
    }

    /** GET /api/donantes/{id}/donaciones?estado=&categoria=&subcategoria=&page=&size= (filtros vacíos se omiten) */
    public Pagina<DonacionDelDonante> donaciones(long donanteId, Map<String, String> filtros) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("donanteId", donanteId);
        return http.get().uri(b -> {
            b.path("/api/donantes/{donanteId}/donaciones");
            filtros.forEach((clave, valor) -> {
                if (valor != null && !valor.isBlank() && !"donanteId".equals(clave)) {
                    // Como variable de plantilla: el valor se codifica y no se interpreta como URI.
                    b.queryParam(clave, "{" + clave + "}");
                    variables.put(clave, valor);
                }
            });
            return b.build(variables);
        }).retrieve().body(new ParameterizedTypeReference<>() { });
    }

    /** GET /api/donantes/{id}/donaciones/resumen: cantidad por estado, con todos los estados. */
    public Map<String, Long> resumenPorEstado(long donanteId) {
        return http.get().uri("/api/donantes/{id}/donaciones/resumen", donanteId)
                .retrieve().body(new ParameterizedTypeReference<>() { });
    }
}
