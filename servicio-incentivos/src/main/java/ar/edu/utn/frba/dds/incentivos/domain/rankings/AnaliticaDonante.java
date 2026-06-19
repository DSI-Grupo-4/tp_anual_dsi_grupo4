package ar.edu.utn.frba.dds.incentivos.domain.rankings;

import ar.edu.utn.frba.dds.incentivos.domain.personas.Donante;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AnaliticaDonante {
    private int totalDonacionesHistoricas;
    private int totalOrganizacionesAyudadas;
    private int posicionRanking;
    private Map<String, Integer> donacionesPorMes;

    public AnaliticaDonante() {}
}
