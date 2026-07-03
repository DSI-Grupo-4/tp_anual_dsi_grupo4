package ar.edu.utn.frba.dds.incentivos.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EvolucionMensualDTO {
    private String mes;
    private int solicitudes;
    private int impacto;
}
