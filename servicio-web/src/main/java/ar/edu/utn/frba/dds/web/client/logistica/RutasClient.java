package ar.edu.utn.frba.dds.web.client.logistica;

import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Cliente de las rutas y del planificador de Logística (seguimiento de la administración y de las entidades). */
@Component
public class RutasClient {

    private final RestClient http;

    public RutasClient(@Qualifier("logistica") RestClient http) {
        this.http = http;
    }

    /** GET /api/rutas */
    public List<RutaApi> rutas() {
        return http.get().uri("/api/rutas").retrieve().body(new ParameterizedTypeReference<>() { });
    }

    /** GET /api/rutas/{id} */
    public RutaApi ruta(Integer id) {
        return http.get().uri("/api/rutas/{id}", id).retrieve().body(RutaApi.class);
    }

    /** POST /api/rutas/{id}/iniciar (409 si la ruta no está PLANIFICADA) */
    public RutaApi iniciar(Integer id) {
        return http.post().uri("/api/rutas/{id}/iniciar", id).retrieve().body(RutaApi.class);
    }

    /** POST /api/planificador/ejecutar: planifica ahora las entregas pendientes y devuelve las rutas nuevas. */
    public List<RutaApi> planificar() {
        return http.post().uri("/api/planificador/ejecutar").retrieve().body(new ParameterizedTypeReference<>() { });
    }

    /**
     * POST /api/rutas/{id}/simular-avance: mueve el camión de una ruta en curso hacia su próxima parada (solo demo).
     * 409 si la ruta no está en curso, no tiene paradas pendientes o la parada no tiene coordenadas.
     */
    public SimulacionApi simularAvance(Integer id) {
        return http.post().uri("/api/rutas/{id}/simular-avance", id).retrieve().body(SimulacionApi.class);
    }

    /**
     * POST /api/rutas/{idRuta}/paradas/{idParada}/recepcion: la entidad confirma la recepción con 1 a 5 fotos (por URL).
     * 409 si la ruta no está en curso o la parada no tiene entregas en traslado; 400 si las fotos no son válidas.
     */
    public void confirmarRecepcion(Integer idRuta, Integer idParada, java.util.List<String> urlsDeFotos) {
        http.post().uri("/api/rutas/{ruta}/paradas/{parada}/recepcion", idRuta, idParada)
                .body(ConfirmacionRecepcionRequest.de(urlsDeFotos))
                .retrieve().toBodilessEntity();
    }
}
