package ar.edu.utn.frba.dds.donaciones.domain.lugares;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class Ciudad {
    @Column(name = "nombre")
    private String nombre;

    @Embedded
    private Provincia provincia;

    public Ciudad(
            String nombre,
            Provincia provincia) {
        this.nombre = nombre;
        this.provincia = provincia;
    }
}
