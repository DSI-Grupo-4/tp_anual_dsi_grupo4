package ar.edu.utn.frba.dds.incentivos.domain.insignias;

import ar.edu.utn.frba.dds.incentivos.domain.personas.Id;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class InsigniasDonante {
    private Integer cantidad;
    private Id id;

    public InsigniasDonante() {
    }

    public InsigniasDonante(Integer cantidad, Id id) {
        this.cantidad = cantidad;
        this.id = id;
    }

}
