package ar.edu.utn.frba.dds.web.client.donaciones;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;

/** Contratos de lectura privados. No se reutilizan para publicar datos personales. */
public final class AdminApi {
    private AdminApi() { }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pagina<T>(List<T> content, long totalElements, int totalPages, int number) { }
    public record Contacto(String tipo, String valor, Boolean esPreferido) { }
    public record Provincia(String nombre) { }
    public record Ciudad(String nombre, Provincia provincia) { }
    public record Direccion(String calle, String numero, Ciudad ciudad) { }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Humana(String nombre, String apellido, Integer edad, String documento, String genero,
                         Direccion direccion, List<Contacto> mediosContacto) { }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Juridica(String razonSocial, String tipo, String rubro, List<Contacto> mediosContacto,
                           List<Humana> representantes) { }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Donante(Long id, Boolean activo, String tipo, String nombre, String apellido,
                           Integer edad, String documento, String genero, String razonSocial,
                           String tipoOrganizacion, String rubro, List<Contacto> mediosContacto,
                           Direccion direccion, List<Humana> representantes) {
        public String etiqueta() { return "JURIDICA".equals(tipo) ? razonSocial : nombre + " " + apellido; }
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Entidad(Long id, Boolean activa, String descripcion, Juridica personaJuridica, Direccion direccion) {
        public String etiqueta() { return personaJuridica.razonSocial(); }
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(String descripcion, String categoria, String subcategoria, String unidadMedida,
                       BigDecimal cantidad, String foto, String condicion, String fechaVencimiento,
                       Integer pesoKg, Integer volumenM3, Integer alturaM) { }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Donacion(Long id, Long donanteId, String descripcionItem, BigDecimal cantidadAsignada,
                            Long solicitudOrigenId, String categoria, String subcategoria, String unidadMedida,
                            List<Item> items, String estadoActual, Long entidadBeneficiariaId, Long necesidadId,
                            java.time.LocalDateTime fechaCreacion) {
        public boolean editable() { return "EN_DEPOSITO".equals(estadoActual); }
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Necesidad(Long id, String tipo, String descripcion, String subcategoria, String unidadMedida,
                             BigDecimal cantidadRequerida, BigDecimal cantidadRecibida, Boolean satisfecha) { }
    public record Historial(String estado, java.time.LocalDateTime fecha, String justificacion) { }
    public record Candidata(Long entidadBeneficiariaId, String razonSocial, String descripcion, Integer puntaje) { }
    public record Resultado(List<Candidata> porCompatibilidad, List<Candidata> porSubatencion, List<Candidata> interseccion) { }
    public record Resumen(long donantesActivos, long entidadesActivas, long enDeposito, long asignadas) { }
    public record Importacion(int cantidadImportados) { }
}
