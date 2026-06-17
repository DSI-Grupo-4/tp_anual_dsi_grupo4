package ar.edu.utn.frba.dds.incentivos.domain.personas;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Id {

    private int codigo;

    public Id(int codigo) {
        this.codigo = codigo;
    }

}
