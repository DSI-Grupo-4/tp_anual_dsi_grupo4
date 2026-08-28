package ar.edu.utn.frba.dds.donaciones.domain.necesidades;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NecesidadRecurrente extends Necesidad {
    private Periodicidad periodicidad;

    public NecesidadRecurrente(
            Long id,
            String descripcion,
            Subcategoria subcategoria,
            Integer cantidadRequerida,
            Periodicidad periodicidad) {

        super(id, descripcion, subcategoria, cantidadRequerida);
        this.periodicidad = periodicidad;
    }
}