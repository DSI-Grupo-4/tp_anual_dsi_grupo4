package ar.edu.utn.frba.dds.incentivos.scheduler;

import ar.edu.utn.frba.dds.incentivos.service.RankingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RankingScheduler {
    private final RankingService rankingService;

    public RankingScheduler(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @Scheduled(cron = "0 0 0 1 * *")
    public void generarRankingMensual() {
        rankingService.generarRankingMensual();
    }
}
