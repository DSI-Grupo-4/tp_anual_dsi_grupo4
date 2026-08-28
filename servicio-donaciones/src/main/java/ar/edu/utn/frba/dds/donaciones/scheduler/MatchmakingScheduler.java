package ar.edu.utn.frba.dds.donaciones.scheduler;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.service.DonacionService;
import ar.edu.utn.frba.dds.donaciones.service.MatchmakingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MatchmakingScheduler {

    private final DonacionService donacionService;
    private final MatchmakingService matchmakingService;

    public MatchmakingScheduler(
            DonacionService donacionService,
            MatchmakingService matchmakingService) {
        this.donacionService = donacionService;
        this.matchmakingService = matchmakingService;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void ejecutarMatchmakingProgramado() {
        List<Donacion> enDeposito = donacionService.obtenerDonacionesEnDeposito();

        for (Donacion donacion : enDeposito) {
            List<EntidadBeneficiaria> candidatas =
                    matchmakingService.ejecutarMatchmaking(donacion);
            donacion.setCandidatas(candidatas);
        }
    }
}
