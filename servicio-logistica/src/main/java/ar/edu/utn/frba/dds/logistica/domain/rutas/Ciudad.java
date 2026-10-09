package ar.edu.utn.frba.dds.logistica.domain.rutas;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Setter
@Embeddable
public class Ciudad {
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
