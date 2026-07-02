package ar.edu.utn.frba.dds.incentivos.ranking;

import ar.edu.utn.frba.dds.incentivos.donante.Donante;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

public class HistorialRanking {

    private static final int TOP_DONANTES = 10;

    private static HistorialRanking instancia;

    private final List<ActividadMensualDonante> donantesMisionesMensuales;
    private final List<Ranking> rankings;

    private HistorialRanking() {
        this.donantesMisionesMensuales = new ArrayList<>();
        this.rankings = new ArrayList<>();
    }

    public static synchronized HistorialRanking getInstance() {
        if (instancia == null) {
            instancia = new HistorialRanking();
        }
        return instancia;
    }

    public void generarRanking() {
        List<ActividadMensualDonante> topDonantes = donantesMisionesMensuales.stream()
                .sorted(Comparator.comparingInt(ActividadMensualDonante::getCantidad).reversed())
                .limit(TOP_DONANTES)
                .toList();

        rankings.add(new Ranking(LocalDate.now(), topDonantes));
        donantesMisionesMensuales.clear();
    }

    public void registrarMisionCompletada(Donante donante) {
        ActividadMensualDonante actividad = donantesMisionesMensuales.stream()
                .filter(a -> a.getDonanteAsociado().getId().equals(donante.getId()))
                .findFirst()
                .orElseGet(() -> {
                    ActividadMensualDonante nueva = new ActividadMensualDonante(donante);
                    donantesMisionesMensuales.add(nueva);
                    return nueva;
                });
        actividad.incrementar();
    }

    public List<Ranking> obtenerHistorial() {
        return List.copyOf(rankings);
    }

    public Ranking obtenerRankingDeMes(LocalDate fecha) {
        return rankings.stream()
                .filter(ranking -> ranking.getFechaEmision().getYear() == fecha.getYear()
                        && ranking.getFechaEmision().getMonth() == fecha.getMonth())
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No existe ranking para el mes solicitado: " + fecha));
    }
}
