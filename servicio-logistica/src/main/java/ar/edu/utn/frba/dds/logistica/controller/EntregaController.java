package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.dto.EntregaDTO;
import ar.edu.utn.frba.dds.logistica.service.EntregaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/entregas")
public class EntregaController {

    private final EntregaService entregaService;

    public EntregaController(EntregaService entregaService) {
        this.entregaService = entregaService;
    }

    @GetMapping("/{id}/estado")
    public EntregaDTO obtenerEstado(@PathVariable Integer id) {
        return entregaService.obtenerEstadoEntrega(id);
    }
}
