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

    @io.swagger.v3.oas.annotations.Operation(summary = "Enviar pendientes", description = "Requiere Logística levantada y donaciones asignadas con dirección y dimensiones completas.")
    @PostMapping("/enviar-pendientes")
    public void enviarPendientes() {
        envioLogisticaScheduler.enviarPendientesALogistica();
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Consumir eventos", description = "Requiere Logística levantada; aplica sus eventos pendientes a las donaciones.")
    @PostMapping("/consumir-eventos")
    public void consumirEventos() {
        eventosLogisticaScheduler.consumirEventos();
    }
}
