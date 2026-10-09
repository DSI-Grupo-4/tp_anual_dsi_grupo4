package ar.edu.utn.frba.dds.logistica.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(example = """
{
  "nombre": "Juan Perez",
  "dni": 30123456,
  "habilitado": true
}
""")
public class ChoferDTO {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Lo asigna el alta, ignorar en el body del POST")
    private Integer idChofer;
    @NotBlank(message = "nombre es obligatorio")
    private String nombre;
    @NotNull(message = "dni es obligatorio")
    @Positive(message = "dni debe ser mayor a 0")
    private Integer dni;
    @NotNull(message = "habilitado es obligatorio")
    private Boolean habilitado;
}
