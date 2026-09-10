package ar.edu.utn.frba.dds.donaciones.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsignarEntidadDTO {
    @NotNull(message = "entidadId es obligatorio")
    private Long entidadId;
}
