package ar.edu.utn.frba.dds.incentivos.domain.rankings;

import ar.edu.utn.frba.dds.incentivos.domain.personas.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Ranking {
    private LocalDate fechaInicio;
    private LocalDate fecha;
    private List<Id> clave = new ArrayList<>();

    public Ranking() {}

    public Ranking(LocalDate fechaInicio, LocalDate fecha, List<Id> clave) {
        this.fechaInicio = fechaInicio;
        this.fecha = fecha;
        this.clave = clave;
    }

    public void generarRanking() {}

    public void agregarInsignia() {}
}
