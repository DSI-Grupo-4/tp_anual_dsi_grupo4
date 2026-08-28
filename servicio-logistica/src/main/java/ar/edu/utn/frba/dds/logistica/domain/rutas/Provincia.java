package ar.edu.utn.frba.dds.logistica.domain.rutas;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter

public class Provincia {
    private String nombre;

    public Provincia(String nombre) {
        this.nombre = nombre;
    }
}
