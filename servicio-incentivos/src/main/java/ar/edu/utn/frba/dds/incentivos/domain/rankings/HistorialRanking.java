package ar.edu.utn.frba.dds.incentivos.domain.rankings;

import java.util.List;
import java.util.ArrayList;
import ar.edu.utn.frba.dds.incentivos.domain.insignias.InsigniasDonante;

public class HistorialRanking {
    private List<InsigniasDonante> donantesMisionesMensuales;
    private List<Ranking> rankings;

    public HistorialRanking() {
        this.rankings = new ArrayList<>();
        this.donantesMisionesMensuales = new ArrayList<>();
    }

    private static HistorialRanking instance = null;
    public static HistorialRanking GetInstance(){
        if (instance == null)
        instance = new HistorialRanking();
        return instance;
    }

    public void generarRanking(){}

    public void asignarInsignia(){}
}
