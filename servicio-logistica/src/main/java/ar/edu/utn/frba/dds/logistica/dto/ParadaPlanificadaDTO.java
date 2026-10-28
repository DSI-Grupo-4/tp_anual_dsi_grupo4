package ar.edu.utn.frba.dds.logistica.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ParadaPlanificadaDTO {
    @NotEmpty(message = "entregasIds no puede estar vacío")
    private List<Integer> entregasIds;
}
