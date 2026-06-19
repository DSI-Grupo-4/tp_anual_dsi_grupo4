package ar.edu.utn.frba.dds.incentivos.service;

import ar.edu.utn.frba.dds.incentivos.domain.personas.Id;
import ar.edu.utn.frba.dds.incentivos.domain.rankings.Ranking;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RankingService {

    private final CuentaDonanteService cuentaDonanteService;
    private final List<Ranking> historial = new ArrayList<>();
    private Ranking rankingActual;

    public RankingService(CuentaDonanteService cuentaDonanteService) {
        this.cuentaDonanteService = cuentaDonanteService;
    }

    public void generarRankingMensual() {
        LocalDate hoy = LocalDate.now();
        List<Id> clave = cuentaDonanteService.obtenerTodas().stream()
                .sorted(Comparator.comparingInt(d -> -d.getTotalDonaciones()))
                .map(d -> new Id(d.getId()))
                .toList();
        Ranking nuevo = new Ranking(hoy.withDayOfMonth(1), hoy, clave);
        if (rankingActual != null) historial.add(rankingActual);
        rankingActual = nuevo;
    }

    public Ranking obtenerActual() {
        return rankingActual;
    }

    public List<Ranking> obtenerHistorial() {
        return historial;
    }
}
