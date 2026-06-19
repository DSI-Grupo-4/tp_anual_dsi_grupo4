package ar.edu.utn.frba.dds.donaciones.scheduler;

import ar.edu.utn.frba.dds.donaciones.repository.DonanteRepository;
import ar.edu.utn.frba.dds.donaciones.service.DonacionesService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DonantesInactivosScheduler {

    private final DonanteRepository donanteRepository;
    private final DonacionesService donacionesService;

    public DonantesInactivosScheduler(DonanteRepository donanteRepository, DonacionesService donacionesService) {
        this.donanteRepository = donanteRepository;
        this.donacionesService = donacionesService;
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void notificarDonantesInactivos() {
        donacionesService.notificarDonantesInactivos(donanteRepository.listarTodos());
    }
}
