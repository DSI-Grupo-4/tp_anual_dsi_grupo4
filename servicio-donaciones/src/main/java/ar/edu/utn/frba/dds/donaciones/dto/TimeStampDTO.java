package ar.edu.utn.frba.dds.donaciones.dto;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoDonacion;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TimeStampDTO {
    private EstadoDonacion estado;
    private LocalDateTime fecha;
    private String justificacion;
}
