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

    /** GET /api/entidades/{id}/donaciones: las de esa entidad. La API rechaza una entidad que no es del token. */
    public List<DonacionApi> donacionesDeEntidad(Long entidadId) {
        return http.get().uri("/api/entidades/{id}/donaciones", entidadId)
                .retrieve().body(new ParameterizedTypeReference<>() { });
    }

    /** GET /api/donantes (solo se leen id y activo) */
    public List<DonanteApi> donantes() {
        return http.get().uri("/api/donantes").retrieve().body(new ParameterizedTypeReference<>() { });
    }

    /** GET /api/entidades */
    public List<EntidadApi> entidades() {
        return http.get().uri("/api/entidades").retrieve().body(new ParameterizedTypeReference<>() { });
    }

    /** GET /api/donaciones/{id} */
    public DonacionApi donacion(Long id) {
        return http.get().uri("/api/donaciones/{id}", id).retrieve().body(DonacionApi.class);
    }

    /** GET /api/donaciones/{id}/historial (fechas en UTC) */
    public List<HistorialEstadoApi> historial(Long id) {
        return http.get().uri("/api/donaciones/{id}/historial", id)
                .retrieve().body(new ParameterizedTypeReference<>() { });
    }

    /** POST /api/cuentas/vincular: asocia la cuenta del token con el donante o las entidades de su email. */
    public Vinculacion vincularCuenta() {
        return http.post().uri("/api/cuentas/vincular").retrieve().body(Vinculacion.class);
    }

    /** POST /api/cuentas/registro/donante. El correo asociado lo impone la API con el token. */
    public void registrarDonante(Object datos) {
        http.post().uri("/api/cuentas/registro/donante").body(datos).retrieve().toBodilessEntity();
    }

    /** POST /api/cuentas/registro/donante-juridico. Un único representante: el correo del token. */
    public void registrarDonanteJuridico(Object datos) {
        http.post().uri("/api/cuentas/registro/donante-juridico").body(datos).retrieve().toBodilessEntity();
    }

    /** POST /api/cuentas/registro/entidad. El primer representante es el correo del token. */
    public void registrarEntidad(Object datos) {
        http.post().uri("/api/cuentas/registro/entidad").body(datos).retrieve().toBodilessEntity();
    }

    /** POST /api/entidades/{id}/representantes. */
    public void agregarRepresentante(Long id, Object datos) {
        http.post().uri("/api/entidades/{id}/representantes", id).body(datos).retrieve().toBodilessEntity();
    }

    /** GET /api/entidades/{id}, para mostrar el nombre en el alta de representantes. */
    public EntidadApi entidad(Long id) {
        return http.get().uri("/api/entidades/{id}", id).retrieve().body(EntidadApi.class);
    }

    public record Vinculacion(Long donanteId, List<Long> entidadIds) {
        public boolean vinculo() {
            return donanteId != null || (entidadIds != null && !entidadIds.isEmpty());
        }
    }
}
