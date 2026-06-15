package ar.edu.utn.frba.dds.donaciones.dto;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.TimeStamp;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class DonacionDTO {
        private Long itemId;
        private Integer cantidad;
        private Long necesidadId;
        private Long entidadId;

        private String estadoActual;
        private LocalDateTime fechaCreacion;
        private List<TimeStampDTO> historial;
}