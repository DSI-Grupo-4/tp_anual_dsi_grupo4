package ar.edu.utn.frba.dds.donaciones.domain.necesidades;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NecesidadRecurrente extends Necesidad {
    private Periodicidad periodicidad;
}
