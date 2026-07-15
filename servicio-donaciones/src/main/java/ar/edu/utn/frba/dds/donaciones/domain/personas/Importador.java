package ar.edu.utn.frba.dds.donaciones.domain.personas;

import java.util.ArrayList;
import java.util.List;

public abstract class Importador {
    protected String nombre;
    protected List<Donante> listaDonantes = new ArrayList<>();

    public String getNombre() {
        return nombre;
    }

    public List<Donante> getListaDonantes() {
        return listaDonantes;
    }

    public abstract void importar(String ruta);
}
