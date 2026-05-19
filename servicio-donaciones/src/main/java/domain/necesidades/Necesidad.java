package domain.necesidades;

import domain.categorias.Subcategoria;

public abstract class Necesidad {
    private String descripcion;
    private Subcategoria subcategoria;
    private Integer cantidadRequerida;
    private Integer cantidadRecibida;

    public boolean satisfecha() {
    return false;
}
}
