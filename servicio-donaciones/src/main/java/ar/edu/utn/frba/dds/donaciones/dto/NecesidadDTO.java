package ar.edu.utn.frba.dds.donaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Periodicidad;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.TipoExtraordinario;
import lombok.Getter;
import java.math.BigDecimal;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.*;
import jakarta.validation.constraints.NotNull;
import lombok.Setter;

@Getter
@Setter
@Schema(example = """
{
  "tipo": "EXTRAORDINARIA",
  "descripcion": "Se necesitan sillas",
  "subcategoria": "SILLA",
  "unidadMedida": "UNIDAD",
  "cantidadRequerida": 3,
  "tipoExtraordinario": "INUNDACION"
}
""")
public class NecesidadDTO {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    private String tipo;
    private String descripcion;
    private Subcategoria subcategoria;
    @NotNull private UnidadMedida unidadMedida;
    private BigDecimal cantidadRequerida;
    private BigDecimal cantidadRecibida;
    private Boolean satisfecha;
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Se toma de entidadId en la URL")
    private Long entidadBeneficiariaId;
    private Periodicidad periodicidad;
    private TipoExtraordinario tipoExtraordinario;
}
