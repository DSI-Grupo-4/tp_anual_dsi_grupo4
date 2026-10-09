package ar.edu.utn.frba.dds.incentivos.dto;

import lombok.Getter;
import java.math.BigDecimal;
import lombok.Setter;

@Getter
@Setter
public class EvolucionMensualDTO {
    private String mes;
    private int solicitudes;
    private BigDecimal impacto;
}
