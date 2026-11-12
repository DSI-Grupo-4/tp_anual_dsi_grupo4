package ar.edu.utn.frba.dds.logistica.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
          "latitud": -34.6037,
          "longitud": -58.3816
        }
        """)
public class UbicacionRequestDTO {

    @NotNull(message = "latitud es obligatoria")
    @DecimalMin(value = "-90.0", message = "latitud debe estar entre -90 y 90")
    @DecimalMax(value = "90.0", message = "latitud debe estar entre -90 y 90")
    private Double latitud;

    @NotNull(message = "longitud es obligatoria")
    @DecimalMin(value = "-180.0", message = "longitud debe estar entre -180 y 180")
    @DecimalMax(value = "180.0", message = "longitud debe estar entre -180 y 180")
    private Double longitud;
}
