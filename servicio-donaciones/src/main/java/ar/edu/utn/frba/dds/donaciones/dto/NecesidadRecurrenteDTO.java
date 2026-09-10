package ar.edu.utn.frba.dds.donaciones.dto;

import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Periodicidad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NecesidadRecurrenteDTO {
    @NotBlank(message = "descripcion es obligatoria")
    private String descripcion;
    @NotBlank(message = "subcategoria es obligatoria")
    private String subcategoria;
    @Positive(message = "cantidadRequerida debe ser mayor a 0")
    private Integer cantidadRequerida;
    private Long entidadBeneficiariaId;
    @NotNull(message = "periodicidad es obligatoria")
    private Periodicidad periodicidad;
}
