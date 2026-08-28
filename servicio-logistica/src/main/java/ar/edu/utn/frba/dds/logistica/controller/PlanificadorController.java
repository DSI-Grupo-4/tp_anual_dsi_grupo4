package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Ruta;
import ar.edu.utn.frba.dds.logistica.service.PlanificadorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/planificador")
public class PlanificadorController {

    private final PlanificadorService planificadorService;

    public PlanificadorController(PlanificadorService planificadorService) {
        this.planificadorService = planificadorService;
    }

    // dispara manualmente lo que normalmente hace el scheduler de madrugada (útil para probar por Postman)
    @PostMapping("/ejecutar")
    public List<Ruta> ejecutarPlanificacion() {
        return planificadorService.planificarRutasDelDia();
    }
}
