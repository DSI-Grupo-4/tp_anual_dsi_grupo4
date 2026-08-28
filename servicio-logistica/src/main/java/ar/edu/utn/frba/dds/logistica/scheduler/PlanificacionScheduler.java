package ar.edu.utn.frba.dds.logistica.scheduler;

import ar.edu.utn.frba.dds.logistica.service.PlanificadorService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PlanificacionScheduler {

    private final PlanificadorService planificadorService;

    public PlanificacionScheduler(PlanificadorService planificadorService) {
        this.planificadorService = planificadorService;
    }

    // horario de baja carga, ej. 3am — ajustable en application.properties
    @Scheduled(cron = "${logistica.planificacion.cron:0 0 3 * * *}")
    public void ejecutarPlanificacionDiaria() {
        planificadorService.planificarRutasDelDia();
    }
}