package ar.edu.utn.frba.dds.donaciones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ItemDonadoDTO {
    @NotBlank(message = "descripcion es obligatoria")
    private String descripcion;
    @NotBlank(message = "subcategoria es obligatoria")
    private String subcategoria;
    @Positive(message = "cantidad debe ser mayor a 0")
    private Integer cantidad;
    private String foto;
    private Integer pesoKg;
    private Integer volumenM3;
    private Integer alturaM;
    // Valores de atributos dinámicos de la subcategoría (ej.
    // "fechaVencimiento" -> "2027-01-01", "estadoUso" -> "USADO"). Ver D-002.
    private Map<String, String> valoresAtributos;
}
