package ar.edu.utn.frba.dds.donaciones.domain.categorias;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Categoria {
    private String nombre;
    private List<SubCategoria> subCategorias = new ArrayList<>();

    public Categoria() {
    }

    public Categoria(String nombre, List<SubCategoria> subCategorias) {
        this.nombre = nombre;
        this.subCategorias = subCategorias != null ? subCategorias : new ArrayList<>();
    }
}
