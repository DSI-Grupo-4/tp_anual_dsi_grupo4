package ar.edu.utn.frba.dds.donaciones.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
@Getter @Setter
@Schema(example = """
{
  "descripcion": "Sillas de oficina usadas",
  "categoria": "MOBILIARIO",
  "subcategoria": "SILLA",
  "unidadMedida": "UNIDAD",
  "cantidad": 3,
  "condicion": "USADO",
  "foto": "https://example.org/sillas.jpg",
  "pesoKg": 15,
  "volumenM3": 2,
  "alturaM": 1
}
""")
public class ItemDonadoDTO {
    @NotBlank private String descripcion;
    @NotNull private Categoria categoria;
    @NotNull private Subcategoria subcategoria;
    @NotNull private UnidadMedida unidadMedida;
    @NotNull @Positive private BigDecimal cantidad;
    private String foto;
    @Schema(description = "Obligatoria en MOBILIARIO y VESTIMENTA; omitir en ALIMENTOS.")
    private Condicion condicion;
    @Schema(description = "Obligatoria para los alimentos del catálogo; omitir en otras categorías.")
    private LocalDate fechaVencimiento;
    @Schema(description = "Peso total de este renglón, no por unidad. Obligatorio antes de asignar.")
    @Positive private Integer pesoKg;
    @Schema(description = "Volumen total de este renglón, no por unidad. Obligatorio antes de asignar.")
    @Positive private Integer volumenM3;
    @Schema(description = "Altura máxima del renglón. Obligatoria antes de asignar.")
    @Positive private Integer alturaM;
}
