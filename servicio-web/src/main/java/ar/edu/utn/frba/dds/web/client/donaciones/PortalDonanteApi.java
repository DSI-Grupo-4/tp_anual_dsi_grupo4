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

    /** EntidadBeneficiariaDTO: solo el nombre, para mostrar a quién fue la donación. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Entidad(Long id, String descripcion, PersonaJuridica personaJuridica) {
        public String nombre() {
            return personaJuridica != null && personaJuridica.razonSocial() != null
                    ? personaJuridica.razonSocial() : "Entidad #" + id;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PersonaJuridica(String razonSocial) {
    }

    /** PaginaDTO. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pagina<T>(List<T> content, long totalElements, int totalPages, int number) {
        public List<T> content() {
            return content == null ? List.of() : content;
        }
    }
}
