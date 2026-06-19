package ar.edu.utn.frba.dds.donaciones.domain.categorias;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Subcategoria {
    private String nombre;
    private List<DefinicionAtributo> atributosAsociados = new ArrayList<>();

    public Subcategoria() {
    }

    public Subcategoria(String nombre) {
        this.nombre = nombre;
    }

    public void agregarAtributo(DefinicionAtributo atributo) {
        atributosAsociados.add(atributo);
    }
}