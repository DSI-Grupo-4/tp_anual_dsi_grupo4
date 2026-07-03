package ar.edu.utn.frba.dds.donaciones.domain.lugares;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Provincia {
    private String nombre;

    public Provincia(String nombre) {
        this.nombre = nombre;
    }
}
