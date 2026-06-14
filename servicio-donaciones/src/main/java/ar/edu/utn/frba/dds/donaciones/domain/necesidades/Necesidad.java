package ar.edu.utn.frba.dds.donaciones.domain.necesidades;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;

public abstract class Necesidad {
    private String descripcion;
    private Subcategoria subcategoria;
    private Integer cantidadRequerida;
    private Integer cantidadRecibida;

    public boolean satisfecha() {
        return cantidadRecibida >= cantidadRequerida;
    }

    public void recibir(Integer cantidad) {
        cantidadRecibida += cantidad;
    }
}
