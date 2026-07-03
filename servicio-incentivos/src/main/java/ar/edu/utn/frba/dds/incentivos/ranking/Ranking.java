package ar.edu.utn.frba.dds.incentivos.ranking;

import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class Ranking {

    private final LocalDate fechaEmision;
    private final List<ActividadMensualDonante> topDonantes;

    public Ranking(LocalDate fechaEmision, List<ActividadMensualDonante> topDonantes) {
        this.fechaEmision = fechaEmision;
        this.topDonantes = topDonantes;
    }
}
