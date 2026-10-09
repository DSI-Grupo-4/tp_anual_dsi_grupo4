package ar.edu.utn.frba.dds.incentivos.ranking;

import ar.edu.utn.frba.dds.incentivos.donante.Donante;
import ar.edu.utn.frba.dds.incentivos.repository.ActividadMensualDonanteRepository;
import ar.edu.utn.frba.dds.incentivos.repository.RankingRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class HistorialRanking {

    private static final int TOP_DONANTES = 3;

    private static HistorialRanking instancia;

    private final List<ActividadMensualDonante> donantesMisionesMensuales;
    private final List<Ranking> rankings;

    // Inyectados al boot por PersistenciaConfigurer -- null en los tests
    // unitarios que no levantan contexto de Spring (se degrada a memoria).
    private RankingRepository rankingRepository;
    private ActividadMensualDonanteRepository actividadMensualDonanteRepository;

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

    public synchronized void configurarPersistencia(RankingRepository rankingRepository,
                                                      ActividadMensualDonanteRepository actividadMensualDonanteRepository) {
        this.rankingRepository = rankingRepository;
        this.actividadMensualDonanteRepository = actividadMensualDonanteRepository;
        rankings.clear();
        rankings.addAll(rankingRepository.findAllByOrderByFechaEmisionAsc());
        donantesMisionesMensuales.clear();
        donantesMisionesMensuales.addAll(actividadMensualDonanteRepository.findByRankingIsNull());
    }

    public void generarRanking() {
        List<ActividadMensualDonante> topDonantes = donantesMisionesMensuales.stream()
                .sorted(Comparator.comparingInt(ActividadMensualDonante::getCantidad).reversed())
                .limit(TOP_DONANTES)
                .toList();

        Ranking ranking = new Ranking(LocalDate.now(), topDonantes);
        rankings.add(ranking);
        donantesMisionesMensuales.clear();
        if (rankingRepository != null) {
            rankingRepository.save(ranking);
        }
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
        if (actividadMensualDonanteRepository != null) {
            actividadMensualDonanteRepository.save(actividad);
        }
    }

    public Optional<Integer> obtenerPosicionActual(Donante donante) {
        List<ActividadMensualDonante> ordenado = donantesMisionesMensuales.stream()
                .sorted(Comparator.comparingInt(ActividadMensualDonante::getCantidad).reversed())
                .toList();

        for (int i = 0; i < ordenado.size(); i++) {
            if (ordenado.get(i).getDonanteAsociado().getId().equals(donante.getId())) {
                return Optional.of(i + 1);
            }
        }
        return Optional.empty();
    }

    public List<Ranking> obtenerHistorial() {
        return List.copyOf(rankings);
    }

    public Ranking obtenerUltimoRanking() {
        if (rankings.isEmpty()) {
            throw new NoSuchElementException("Todavía no se generó ningún ranking mensual");
        }
        return rankings.get(rankings.size() - 1);
    }

    public Ranking obtenerRankingDeMes(LocalDate fecha) {
        return rankings.stream()
                .filter(ranking -> ranking.getFechaEmision().getYear() == fecha.getYear()
                        && ranking.getFechaEmision().getMonth() == fecha.getMonth())
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No existe ranking para el mes solicitado: " + fecha));
    }
}
