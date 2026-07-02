package ar.edu.utn.frba.dds.incentivos.scheduler;

import ar.edu.utn.frba.dds.incentivos.consultor.Consultor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IncentivosScheduler {

    private final Consultor consultor = Consultor.getInstance();

    @Scheduled(cron = "0 0 3 * * *")
    public void ejecutarVerificacionVigenciaMisiones() {
        consultor.ejecutarVerificacionVigenciaMisiones();
    }

    @Scheduled(cron = "0 0 1 1 * *")
    public void ejecutarCierreMensualRanking() {
        consultor.ejecutarCierreMensualRanking();
    }
}
