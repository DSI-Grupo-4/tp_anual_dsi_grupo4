package ar.edu.utn.frba.dds.incentivos.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PublicacionInsigniaDTO {
    private Long donanteId;
    private String donanteNombre;
    private String insigniaNombre;
    private String imagenUrl;
    private LocalDate fechaObtencion;
}
