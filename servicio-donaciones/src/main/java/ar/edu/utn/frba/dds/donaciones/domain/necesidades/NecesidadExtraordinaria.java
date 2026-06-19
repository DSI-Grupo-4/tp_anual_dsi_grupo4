package ar.edu.utn.frba.dds.donaciones.domain.necesidades;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NecesidadExtraordinaria extends Necesidad {
    private TipoExtraordinario tipoExtraordinario;
}
