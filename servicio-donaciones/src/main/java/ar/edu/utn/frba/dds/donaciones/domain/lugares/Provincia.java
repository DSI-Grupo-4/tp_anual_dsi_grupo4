package ar.edu.utn.frba.dds.donaciones.domain.lugares;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class Provincia {
    @Column(name = "nombre")
    private String nombre;

    public Provincia(String nombre) {
        this.nombre = nombre;
    }
}
