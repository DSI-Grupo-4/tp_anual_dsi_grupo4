package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.scheduler.EnvioLogisticaScheduler;
import ar.edu.utn.frba.dds.donaciones.scheduler.EventosLogisticaScheduler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dispara manualmente lo que normalmente ejecutan EnvioLogisticaScheduler y
 * EventosLogisticaScheduler de madrugada (mismo patrón que
 * PlanificadorController en Logística: útil para probar por Bruno/Postman
 * sin esperar al cron).
 */
@RestController
@RequestMapping("/api/logistica")
public class LogisticaIntegracionController {

    private final EnvioLogisticaScheduler envioLogisticaScheduler;
    private final EventosLogisticaScheduler eventosLogisticaScheduler;

    public LogisticaIntegracionController(
            EnvioLogisticaScheduler envioLogisticaScheduler,
            EventosLogisticaScheduler eventosLogisticaScheduler) {
        this.envioLogisticaScheduler = envioLogisticaScheduler;
        this.eventosLogisticaScheduler = eventosLogisticaScheduler;
    }

    @PostMapping("/enviar-pendientes")
    public void enviarPendientes() {
        envioLogisticaScheduler.enviarPendientesALogistica();
    }

    @PostMapping("/consumir-eventos")
    public void consumirEventos() {
        eventosLogisticaScheduler.consumirEventos();
    }
}
