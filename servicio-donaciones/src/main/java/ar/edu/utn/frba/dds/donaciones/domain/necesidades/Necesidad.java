package ar.edu.utn.frba.dds.donaciones.domain.necesidades;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.SubCategoria;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Necesidad {
    private String descripcion;
    private int cantidadRequerida;
    private SubCategoria subcategoria;
    private SubCategoria categoriaAñadida;

    public void agregarNecesidad() {
    }

    public void registrarAyuda() {
    }
}
