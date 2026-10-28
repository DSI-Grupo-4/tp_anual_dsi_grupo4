package ar.edu.utn.frba.dds.logistica.domain.rutas;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Se embebe en Entrega y en Parada; los nombres de columna se fijan con @AttributeOverrides en cada una.
@Getter
@NoArgsConstructor
@Setter
@Embeddable
public class Direccion {
    private String calle;
    private String numero;

    @Embedded
    private Ciudad ciudad;

    public Direccion(
            String calle,
            String numero,
            Ciudad ciudad) {

        this.calle = calle;
        this.numero = numero;
        this.ciudad = ciudad;
    }
}
