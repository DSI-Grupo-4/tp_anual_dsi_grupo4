package ar.edu.utn.frba.dds.donaciones.domain.categorias;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Categoria {
    private String nombre;
    private List<Subcategoria> subcategorias = new ArrayList<>();

    public Categoria() {
    }

    public Categoria(String nombre, List<Subcategoria> subcategorias) {
        this.nombre = nombre;
        this.subcategorias = subcategorias != null ? subcategorias : new ArrayList<>();
    }
}
