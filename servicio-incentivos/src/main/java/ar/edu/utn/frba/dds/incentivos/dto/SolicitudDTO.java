package ar.edu.utn.frba.dds.incentivos.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SolicitudDTO {
    private int donanteId;
    private int cantItems;
    private int cantCategorias;
    private LocalDate fecha;
}
