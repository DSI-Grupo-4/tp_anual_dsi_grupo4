package ar.edu.utn.frba.dds.logistica.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RutaPlanificadaDTO {
    @NotNull(message = "idCamion es obligatorio")
    private Integer idCamion;
    @Schema(description = "Opcional -- si no se indica, la ruta queda sin chofer asignado hasta que se le asigne uno.")
    private Integer idChofer;
    @NotEmpty(message = "paradas no puede estar vacío")
    private List<@NotNull @Valid ParadaPlanificadaDTO> paradas;
}
