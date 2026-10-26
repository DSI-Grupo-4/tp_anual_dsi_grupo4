package ar.edu.utn.frba.dds.logistica.domain.rutas;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Value object sin identidad: se persiste embebido dentro de Ciudad.
@Getter
@NoArgsConstructor
@Setter
public class Provincia {

    private String nombre;

    public Provincia(String nombre) {
        this.nombre = nombre;
    }
}
