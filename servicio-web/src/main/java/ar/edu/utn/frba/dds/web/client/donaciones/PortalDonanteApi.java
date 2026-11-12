package ar.edu.utn.frba.dds.web.client.donaciones;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Respuestas del servicio de Donaciones que usa el portal del donante (Persona 4). */
public final class PortalDonanteApi {

    private PortalDonanteApi() {
    }

    /** DonanteDTO: solo lo necesario para saludar y saber si es persona humana o jurídica. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Perfil(Long id, Boolean activo, String tipo, String nombre, String apellido, String razonSocial) {
        public boolean esHumana() {
            return !"JURIDICA".equals(tipo);
        }

        public String nombreVisible() {
            return esHumana() ? nombre : razonSocial;
        }
    }

    /** DonacionDTO, en el portal del donante. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DonacionDelDonante(Long id, LocalDateTime fechaCreacion, String descripcionItem,
                                     BigDecimal cantidadAsignada, String unidadMedida, String categoria,
                                     String subcategoria, String estadoActual, Long entidadBeneficiariaId) {
    }

    /** DonacionDTO completo, para el detalle: incluye los bienes. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DetalleDonacion(Long id, Long donanteId, LocalDateTime fechaCreacion, String descripcionItem,
                                  BigDecimal cantidadAsignada, String unidadMedida, String categoria,
                                  String subcategoria, String estadoActual, Long entidadBeneficiariaId,
                                  List<Bien> items) {
        public List<Bien> items() {
            return items == null ? List.of() : items;
        }
    }

    /** ItemDonadoDTO. condicion es null en Alimentos. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Bien(String descripcion, String categoria, String subcategoria, String unidadMedida,
                       BigDecimal cantidad, String condicion, String fechaVencimiento) {
    }

    /** TimeStampDTO: un cambio de estado. fecha en UTC (D-037). */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CambioEstado(String estado, LocalDateTime fecha, String justificacion) {
    }

    /**
     * EntidadBeneficiariaDTO, en el portal del donante. Solo datos que puede ver un donante:
     * sin contactos, representantes ni calle.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Entidad(Long id, Boolean activa, String descripcion, PersonaJuridica personaJuridica, Direccion direccion) {
        public String nombre() {
            return personaJuridica != null && personaJuridica.razonSocial() != null
                    ? personaJuridica.razonSocial() : "Entidad #" + id;
        }

        public boolean estaActiva() {
            return !Boolean.FALSE.equals(activa);
        }

        public String tipo() {
            return personaJuridica == null ? null : personaJuridica.tipo();
        }

        public String rubro() {
            return personaJuridica == null ? null : personaJuridica.rubro();
        }

        /** "Ciudad, Provincia", o null si no hay datos. */
        public String ubicacion() {
            if (direccion == null || direccion.ciudad() == null) {
                return null;
            }
            String ciudad = direccion.ciudad().nombre();
            String provincia = direccion.ciudad().provincia() == null ? null : direccion.ciudad().provincia().nombre();
            if (ciudad == null || ciudad.isBlank()) {
                return provincia;
            }
            return provincia == null || provincia.isBlank() || provincia.equalsIgnoreCase(ciudad)
                    ? ciudad : ciudad + ", " + provincia;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PersonaJuridica(String razonSocial, String tipo, String rubro) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Direccion(Ciudad ciudad) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Ciudad(String nombre, Provincia provincia) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Provincia(String nombre) {
    }

    /** NecesidadDTO. tipo es RECURRENTE o EXTRAORDINARIA. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Necesidad(Long id, String tipo, String descripcion, String subcategoria, String unidadMedida,
                            BigDecimal cantidadRequerida, BigDecimal cantidadRecibida, Boolean satisfecha,
                            String periodicidad, String tipoExtraordinario) {
        public boolean esExtraordinaria() {
            return "EXTRAORDINARIA".equalsIgnoreCase(tipo);
        }

        public boolean abierta() {
            return !Boolean.TRUE.equals(satisfecha);
        }

        /** Porcentaje cubierto, entre 0 y 100. */
        public int porcentaje() {
            if (cantidadRequerida == null || cantidadRequerida.signum() <= 0 || cantidadRecibida == null) {
                return 0;
            }
            return Math.min(100, cantidadRecibida.multiply(BigDecimal.valueOf(100))
                    .divide(cantidadRequerida, 0, java.math.RoundingMode.DOWN).intValue());
        }
    }

    /** PaginaDTO. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pagina<T>(List<T> content, long totalElements, int totalPages, int number) {
        public List<T> content() {
            return content == null ? List.of() : content;
        }
    }
}
