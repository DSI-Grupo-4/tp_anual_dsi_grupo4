package ar.edu.utn.frba.dds.donaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(example = """
{
  "entidadId": 1
}
""")
public class AsignarEntidadDTO {
    @NotNull(message = "entidadId es obligatorio")
    private Long entidadId;
}
