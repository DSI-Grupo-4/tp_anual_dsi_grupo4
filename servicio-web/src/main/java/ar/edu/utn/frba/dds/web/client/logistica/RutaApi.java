package ar.edu.utn.frba.dds.web.client.logistica;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Ruta con sus paradas y entregas, tal como la devuelve GET /api/rutas de Logística.
 * Del chofer solo se lee el nombre (no el DNI), y de cada entrega solo lo que muestran las pantallas de seguimiento.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RutaApi(
        Integer idRuta,
        LocalDate fecha,
        String estadoRuta,
        CamionApi camionAsociado,
        ChoferApi chofer,
        List<ParadaApi> paradas) {

    private static final Set<String> CERRADAS = Set.of("ENTREGADA", "NO_RECIBIDA", "FALLIDA");

    public List<ParadaApi> paradas() {
        return paradas == null ? List.of() : paradas;
    }

    public List<EntregaDeRutaApi> entregas() {
        return paradas().stream().flatMap(p -> p.entregas().stream()).toList();
    }

    public int totalEntregas() {
        return entregas().size();
    }

    public int entregadas() {
        return (int) entregas().stream().filter(e -> "ENTREGADA".equals(e.estadoEntrega())).count();
    }

    /** Entregas que ya tienen un resultado (recibidas, no recibidas o fallidas). */
    public int resueltas() {
        return (int) entregas().stream().filter(e -> CERRADAS.contains(e.estadoEntrega())).count();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChoferApi(Integer idChofer, String nombre) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ParadaApi(
            Integer idParada,
            Integer numeroParada,
            Integer idEntidadBeneficiariaAsociada,
            DireccionApi direccion,
            List<EntregaDeRutaApi> entregas) {

        public List<EntregaDeRutaApi> entregas() {
            return entregas == null ? List.of() : entregas;
        }

        /** Solo una parada con entregas en traslado admite registrar «no recibida» o un incidente. */
        public boolean tieneEntregasEnTraslado() {
            return entregas().stream().anyMatch(e -> "EN_TRASLADO".equals(e.estadoEntrega()));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EntregaDeRutaApi(
            Integer idEntrega,
            Integer idDonacionAsociada,
            String estadoEntrega,
            LocalDateTime fechaHoraEntrega,
            String justificacionFallo,
            List<EntregaApi.FotoApi> fotos) {

        public List<EntregaApi.FotoApi> fotos() {
            return fotos == null ? List.of() : fotos;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DireccionApi(String calle, String numero, CiudadApi ciudad, Double latitud, Double longitud) {

        /** Dirección sin coordenadas. */
        public DireccionApi(String calle, String numero, CiudadApi ciudad) {
            this(calle, numero, ciudad, null, null);
        }

        public boolean tieneCoordenadas() {
            return latitud != null && longitud != null;
        }

        /** «Calle 123, Ciudad» sin los datos que falten. */
        public String texto() {
            StringBuilder sb = new StringBuilder();
            if (calle != null && !calle.isBlank()) {
                sb.append(calle.trim());
            }
            if (numero != null && !numero.isBlank()) {
                sb.append(sb.length() > 0 ? " " : "").append(numero.trim());
            }
            if (ciudad != null && ciudad.nombre() != null && !ciudad.nombre().isBlank()) {
                sb.append(sb.length() > 0 ? ", " : "").append(ciudad.nombre().trim());
            }
            return sb.toString();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CiudadApi(String nombre) {
    }
}
