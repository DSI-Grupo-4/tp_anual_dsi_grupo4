package ar.edu.utn.frba.dds.incentivos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(example = """
{
  "visible": true
}
""")
public class VisibilidadInsigniaDTO {
    private boolean visible;
}
