package ar.edu.utn.frba.dds.donaciones.domain.categorias;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubCategoria {
    private String nombre;

    public SubCategoria() {
    }

    public SubCategoria(String nombre) {
        this.nombre = nombre;
    }
}