package ar.edu.utn.frba.dds.donaciones.domain.categorias;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class SubCategoria {
    protected String nombreSubcategoria;
    protected List<DefinicionAtributo> atributosAsociados = new ArrayList<>();
    protected int cantidad;

    public void agregarAtributo(DefinicionAtributo atributo) {
        atributosAsociados.add(atributo);
    }
}
