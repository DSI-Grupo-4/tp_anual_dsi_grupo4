package ar.edu.utn.frba.dds.incentivos.domain.rankings;

import java.util.ArrayList;
import java.util.List;

public class HistorialRanking {
    private List<Ranking> rankings = new ArrayList<>();

    private static HistorialRanking instance = null;

    public static HistorialRanking getInstance() {
        if (instance == null) instance = new HistorialRanking();
        return instance;
    }

    public void generarRanking() {}

    public void asignarInsignia() {}

    public List<Ranking> getRankings() { return rankings; }
}
