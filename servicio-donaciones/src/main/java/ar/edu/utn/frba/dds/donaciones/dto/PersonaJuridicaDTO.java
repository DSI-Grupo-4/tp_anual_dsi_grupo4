package ar.edu.utn.frba.dds.donaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoOrganizacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(example = """
{
  "razonSocial": "Comedor Sonrisas",
  "tipo": "ONG",
  "rubro": "Asistencia alimentaria"
}
""")
public class PersonaJuridicaDTO {
    @NotBlank(message = "razonSocial es obligatoria")
    private String razonSocial;
    @NotNull(message = "tipo es obligatorio")
    private TipoOrganizacion tipo;
    private String rubro;
}
