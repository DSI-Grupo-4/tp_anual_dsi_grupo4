package ar.edu.utn.frba.dds.incentivos.dto;

import java.time.LocalDate;
import ar.edu.utn.frba.dds.incentivos.domain.personas.Donante;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SolicitudDTO {
    private Donante donanteAsignado;
    private Integer cantItems;
    private Integer cantCategorias;
    private LocalDate fecha;
}
