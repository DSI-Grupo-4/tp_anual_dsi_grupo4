package ar.edu.utn.frba.dds.donaciones.domain.categorias;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Subcategoria {
    private String nombre;

    public Subcategoria() {
    }

    public Subcategoria(String nombre) {
        this.nombre = nombre;
    }
}