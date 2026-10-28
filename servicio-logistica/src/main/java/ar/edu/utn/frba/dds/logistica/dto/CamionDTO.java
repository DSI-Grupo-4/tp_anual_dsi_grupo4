package ar.edu.utn.frba.dds.logistica.dto;

import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoCamion;
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
  "patente": "AA123BB",
  "capacidadVolumenM3": 20,
  "alturaM": 3,
  "capacidadCargaKg": 1500,
  "estadoCamion": "DISPONIBLE"
}
""")
public class CamionDTO {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Lo asigna el alta, ignorar en el body del POST")
    private Integer idCamion;
    @NotBlank(message = "patente es obligatoria")
    private String patente;
    @NotNull(message = "capacidadVolumenM3 es obligatoria")
    @Positive(message = "capacidadVolumenM3 debe ser mayor a 0")
    private Integer capacidadVolumenM3;
    @NotNull(message = "alturaM es obligatoria")
    @Positive(message = "alturaM debe ser mayor a 0")
    private Integer alturaM;
    @NotNull(message = "capacidadCargaKg es obligatoria")
    @Positive(message = "capacidadCargaKg debe ser mayor a 0")
    private Integer capacidadCargaKg;
    @Schema(description = "Opcional en el alta (default DISPONIBLE); usar el PUT para cambiarlo luego (ej. FUERA_DE_SERVICIO).")
    private EstadoCamion estadoCamion;
}
