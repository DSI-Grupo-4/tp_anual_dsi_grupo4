package ar.edu.utn.frba.dds.donaciones.domain.lugares;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Ciudad {
    private String nombre;
    private Provincia provincia;

    public Ciudad(
            String nombre,
            Provincia provincia) {
        this.nombre = nombre;
        this.provincia = provincia;
    }
}