package ar.edu.utn.frba.dds.incentivos.controller;

import ar.edu.utn.frba.dds.incentivos.domain.rankings.Ranking;
import ar.edu.utn.frba.dds.incentivos.service.RankingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incentivos")
public class RankingController {
    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/ranking/actual")
    public Ranking obtenerActual() {
        return rankingService.obtenerActual();
    }

    @GetMapping("/ranking/historial")
    public List<Ranking> obtenerHistorial() {
        return rankingService.obtenerHistorial();
    }
}
