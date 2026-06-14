package ar.edu.utn.frba.dds.donaciones.domain.necesidades;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;

public class NecesidadRecurrente extends Necesidad{
    private Periodicidad periodicidad;

    public NecesidadRecurrente(String descripcion, Subcategoria subcategoria, Integer cantidadRequerida) {
        super(descripcion, subcategoria, cantidadRequerida);

        this.periodicidad = periodicidad;
    }
}
