package ar.edu.utn.frba.dds.donaciones.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Representa la "única carga": una persona donante (o la persona
 * administradora en su nombre) registra en un solo POST la descripción
 * general y todos los bienes que trae. El servicio se encarga de
 * segmentarla en N Donacion, una por subcategoría
 * (ver SolicitudDonacion.segmentar()).
 */
@Getter
@Setter
public class CargaDonacionDTO {
    @NotNull(message = "donanteId es obligatorio")
    private Long donanteId;
    @NotBlank(message = "descripcion es obligatoria")
    private String descripcion;
    @NotEmpty(message = "la carga debe tener al menos un ítem")
    private List<@Valid ItemDonadoDTO> items;
}
