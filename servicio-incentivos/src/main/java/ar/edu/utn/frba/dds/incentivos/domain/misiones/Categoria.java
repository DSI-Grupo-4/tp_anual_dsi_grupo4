package ar.edu.utn.frba.dds.incentivos.domain.misiones;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class Categoria {


    private String nombre;
    private List<Mision> misiones;
    private Categoria siguiente;

    public Categoria(String nombre, List<Mision> misiones) {
        this.nombre = nombre;
        this.misiones = misiones;
    }

    public Categoria categoriaSiguiente() {
        // TODO: implementar lógica
        return null;
    }

    // Devuelve la primera misión de la lista
    public Mision primeraMision() {
        if (misiones != null && !misiones.isEmpty()) {
            return misiones.get(0);
        }
        return null;
    }
}
