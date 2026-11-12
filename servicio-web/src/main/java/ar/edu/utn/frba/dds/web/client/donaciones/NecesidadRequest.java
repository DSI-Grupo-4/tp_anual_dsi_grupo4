package ar.edu.utn.frba.dds.web.client.donaciones;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

/**
 * Cuerpo para crear o actualizar una necesidad. Sirve para los tres endpoints de Donaciones:
 * los campos que no corresponden al tipo quedan en null y no se envían.
 *
 * @param tipo RECURRENTE o EXTRAORDINARIA (Donaciones lo exige en el PUT)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NecesidadRequest(
        String tipo,
        String descripcion,
        String subcategoria,
        String unidadMedida,
        BigDecimal cantidadRequerida,
        String periodicidad,
        String tipoExtraordinario) {
}
