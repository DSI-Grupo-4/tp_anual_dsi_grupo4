package ar.edu.utn.frba.dds.logistica.domain.rutas;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Value object sin identidad: se persiste embebido (columna definida donde se usa la Direccion).
@Getter
@NoArgsConstructor
@Setter
@Embeddable
public class Provincia {
    private String nombre;

    public Provincia(String nombre) {
        this.nombre = nombre;
    }
}
