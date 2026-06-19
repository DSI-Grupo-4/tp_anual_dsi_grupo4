package ar.edu.utn.frba.dds.incentivos.domain.misiones;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Categoria {
    private String nombre;
    private List<Mision> misiones = new ArrayList<>();

    public Categoria(String nombre, List<Mision> misiones) {
        this.nombre = nombre;
        this.misiones = misiones;
    }

    public Categoria() {}

    public void crearMision() {}

    public Mision primeraMision() {
        if (misiones != null && !misiones.isEmpty()) return misiones.get(0);
        return null;
    }
}
