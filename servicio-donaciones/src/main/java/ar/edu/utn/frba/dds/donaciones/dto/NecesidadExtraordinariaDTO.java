package ar.edu.utn.frba.dds.donaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.TipoExtraordinario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import java.math.BigDecimal;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.*;
import lombok.Setter;

@Getter
@Setter
@Schema(example = """
{
  "descripcion": "Se necesitan sillas",
  "subcategoria": "SILLA",
  "unidadMedida": "UNIDAD",
  "cantidadRequerida": 3,
  "tipoExtraordinario": "INUNDACION"
}
""")
public class NecesidadExtraordinariaDTO {
    @NotBlank(message = "descripcion es obligatoria")
    private String descripcion;
    @NotNull(message = "subcategoria es obligatoria")
    private Subcategoria subcategoria;
    @NotNull private UnidadMedida unidadMedida;
    @NotNull
    @Positive(message = "cantidadRequerida debe ser mayor a 0")
    private BigDecimal cantidadRequerida;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Se toma de entidadId en la URL")
    private Long entidadBeneficiariaId;
    @NotNull(message = "tipoExtraordinario es obligatorio")
    private TipoExtraordinario tipoExtraordinario;
}
