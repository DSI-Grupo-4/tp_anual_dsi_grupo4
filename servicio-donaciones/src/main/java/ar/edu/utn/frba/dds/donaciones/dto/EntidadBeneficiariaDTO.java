package ar.edu.utn.frba.dds.donaciones.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(example = """
{
  "personaJuridica": {
    "razonSocial": "Comedor Sonrisas",
    "tipo": "ONG",
    "rubro": "Asistencia alimentaria",
    "mediosContacto": [{"tipo": "EMAIL", "valor": "comedor@example.org", "esPreferido": true}]
  },
  "descripcion": "Comedor comunitario",
  "direccion": {
    "calle": "Av. San Martin",
    "numero": "1250",
    "ciudad": {
      "nombre": "Cordoba",
      "provincia": {
        "nombre": "Cordoba"
      }
    }
  }
}
""")
public class EntidadBeneficiariaDTO {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    @NotNull(message = "personaJuridica es obligatoria")
    @Valid
    private PersonaJuridicaDTO personaJuridica;
    @NotBlank(message = "descripcion es obligatoria")
    private String descripcion;
    private DireccionDTO direccion;
}
