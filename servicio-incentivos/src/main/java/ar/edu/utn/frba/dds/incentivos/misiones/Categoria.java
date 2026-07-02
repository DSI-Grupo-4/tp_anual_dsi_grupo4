package ar.edu.utn.frba.dds.incentivos.misiones;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Categoria {

    private String nombre;
    private List<Mision> misiones;

    public Categoria(String nombre) {
        this.nombre = nombre;
        this.misiones = new ArrayList<>();
    }

    public void agregarMision(Mision mision) {
        misiones.add(mision);
    }

    public Mision indexMision(int indice) {
        return misiones.get(indice);
    }

    public int cantMisiones() {
        return misiones.size();
    }
}
