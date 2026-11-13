package ar.edu.utn.frba.dds.web.client.donaciones;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

/**
 * Necesidad de una entidad tal como la devuelve Donaciones (NecesidadDTO).
 * Los enums llegan como texto: las etiquetas visibles salen de messages (subcategoria.X, unidad.X, etc.).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NecesidadApi(
        Long id,
        String tipo,
        String descripcion,
        String subcategoria,
        String unidadMedida,
        BigDecimal cantidadRequerida,
        BigDecimal cantidadRecibida,
        Boolean satisfecha,
        String periodicidad,
        String tipoExtraordinario) {

    public boolean recurrente() {
        return "RECURRENTE".equalsIgnoreCase(tipo) || (tipo == null && periodicidad != null);
    }

    public boolean satisfechaOk() {
        return Boolean.TRUE.equals(satisfecha);
    }

    /** Lo que todavía falta cubrir; nunca es negativo. */
    public BigDecimal pendiente() {
        BigDecimal requerida = cantidadRequerida == null ? BigDecimal.ZERO : cantidadRequerida;
        BigDecimal recibida = cantidadRecibida == null ? BigDecimal.ZERO : cantidadRecibida;
        return requerida.subtract(recibida).max(BigDecimal.ZERO);
    }

    /** Las necesidades extraordinarias (inundación, sismo, ola polar) sin cubrir son las urgentes. */
    public boolean urgente() {
        return !recurrente() && !satisfechaOk();
    }
}
