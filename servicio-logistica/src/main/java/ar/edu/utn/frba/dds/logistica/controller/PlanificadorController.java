package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import ar.edu.utn.frba.dds.logistica.dto.ResultadoPlanificacionDTO;
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

    @PostMapping("/procesar")
    public List<Entrega> procesarDonaciones() {
        return planificadorService.procesarDonaciones();
    }

    @PostMapping("/callback")
    public void recibirResultado(@RequestBody ResultadoPlanificacionDTO resultado) {
        planificadorService.procesarResultado(resultado);
    }

}
