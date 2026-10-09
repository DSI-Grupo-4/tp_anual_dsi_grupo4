package ar.edu.utn.frba.dds.donaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Resultado de una importación masiva de donantes: solo la cantidad, no el
 * detalle de cada uno. Devolver los N donantes completos (ej. 20000 en el
 * CSV de ejemplo del repo) no aporta nada que el cliente vaya a usar ahí
 * mismo -- ya puede pedirlos por GET /api/donantes si los necesita -- y
 * tiene un costo real: armar y serializar esa cantidad de DTOs agrega
 * latencia proporcional al tamaño del archivo, no al trabajo real del alta.
 */
@Getter
@AllArgsConstructor
@Schema(example = """
{
  "cantidadImportados": 20000
}
""")
public class ImportacionDonantesDTO {
    private int cantidadImportados;
}
