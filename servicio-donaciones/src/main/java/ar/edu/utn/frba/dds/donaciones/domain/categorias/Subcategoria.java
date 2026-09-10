package ar.edu.utn.frba.dds.donaciones.domain.categorias;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Subcategoria {
    private String nombre;
    private Categoria categoria;
    private UnidadMedida unidadMedida;
    private List<AtributoDefinicion> atributos;

    public Subcategoria() {
        this.atributos = new ArrayList<>();
    }

    public Subcategoria(String nombre) {
        this();
        this.nombre = nombre;
    }

    public Subcategoria(String nombre, Categoria categoria, UnidadMedida unidadMedida) {
        this(nombre);
        this.categoria = categoria;
        this.unidadMedida = unidadMedida;
    }

    public void agregarAtributo(AtributoDefinicion atributo) {
        atributos.add(atributo);
    }
}
