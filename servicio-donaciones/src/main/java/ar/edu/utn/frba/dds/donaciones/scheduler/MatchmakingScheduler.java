package ar.edu.utn.frba.dds.donaciones.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MatchmakingScheduler {

    @Scheduled(cron = "0 0 2 * * *")
    public void ejecutarMatchmakingProgramado() {
        // stub: domain model does not support deposit state or candidatas assignment
    }
}
