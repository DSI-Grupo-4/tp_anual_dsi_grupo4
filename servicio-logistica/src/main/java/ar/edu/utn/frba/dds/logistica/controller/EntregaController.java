package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.dto.EntregaDTO;
import ar.edu.utn.frba.dds.logistica.repository.EntregaRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/entregas")
public class EntregaController {

    private final EntregaRepository entregaRepository;

    public EntregaController(EntregaRepository entregaRepository) {
        this.entregaRepository = entregaRepository;
    }

    @GetMapping("/{id}/estado")
    public EntregaDTO obtenerEstado(@PathVariable Integer id) {
        var entrega = entregaRepository.buscarPorId(id);
        return new EntregaDTO(entrega.getIdEntrega(), entrega.getEstadoEntrega());
    }
}