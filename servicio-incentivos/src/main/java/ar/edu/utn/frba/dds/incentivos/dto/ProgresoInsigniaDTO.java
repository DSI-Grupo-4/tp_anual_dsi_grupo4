package ar.edu.utn.frba.dds.incentivos.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ProgresoInsigniaDTO {
    private String insigniaNombre;
    private String imagenUrl;
    private LocalDate fechaObtencion;
    private boolean visible;
}
