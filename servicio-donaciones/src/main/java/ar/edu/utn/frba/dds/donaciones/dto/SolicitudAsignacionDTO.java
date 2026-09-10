package ar.edu.utn.frba.dds.donaciones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SolicitudAsignacionDTO {
    @NotBlank(message = "descripcionItem es obligatoria")
    private String descripcionItem;
    @NotBlank(message = "subcategoria es obligatoria")
    private String subcategoria;
    @Positive(message = "cantidad debe ser mayor a 0")
    private Integer cantidad;
    private String algoritmo;
}
