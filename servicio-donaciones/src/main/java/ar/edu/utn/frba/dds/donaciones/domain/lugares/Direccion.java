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
public class Direccion {
    @Column(name = "calle")
    private String calle;

    @Column(name = "numero")
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
