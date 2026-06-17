package ar.edu.utn.frba.dds.incentivos.domain.rankings;

import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class Ranking {

    private LocalDate fechaEmision;
    private List<Integer> idsDonantes;

    public Ranking(LocalDate fechaEmision, List<Integer> idsDonantes) {
        this.fechaEmision = fechaEmision;
        this.idsDonantes = idsDonantes;
    }

}
