package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.dto.EntregaDTO;
import ar.edu.utn.frba.dds.logistica.repository.EntregaRepository;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/entregas")
public class EntregaController {

    private final EntregaRepository entregaRepository;

    public EntregaController(EntregaRepository entregaRepository) {
        this.entregaRepository = entregaRepository;
    }

    @Operation(
            summary = "Obtener estado de una entrega",
            description = "Obtiene el estado actual de una entrega a partir de su identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado de la entrega obtenido correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró una entrega con el ID indicado"
            )
    })
    @GetMapping("/{id}/estado")
    public EntregaDTO obtenerEstado(@PathVariable Integer id) {
        var entrega = entregaRepository.buscarPorId(id);
        return new EntregaDTO(entrega.getIdEntrega(), entrega.getEstadoEntrega());
    }
}