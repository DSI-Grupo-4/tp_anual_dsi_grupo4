package ar.edu.utn.frba.dds.incentivos.misiones;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Insignia {

    private String nombre;
    private String imagenUrl;

    public Insignia(String nombre, String imagenUrl) {
        this.nombre = nombre;
        this.imagenUrl = imagenUrl;
    }
}
