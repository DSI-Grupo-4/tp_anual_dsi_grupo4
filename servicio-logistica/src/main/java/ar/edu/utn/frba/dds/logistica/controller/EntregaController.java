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
    public EntregaDTO obtenerEstado(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Integer id) {
        var entrega = entregaRepository.buscarPorId(id);
        return new EntregaDTO(entrega.getIdEntrega(), entrega.getEstadoEntrega());
    }

    @Operation(
            summary = "Reingresar una entrega NO_RECIBIDA al depósito",
            description = "La persona administradora revisó el caso y decide que la donación vuelve al depósito: la entrega pasa a PENDIENTE para que la próxima planificación la vuelva a tomar."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Entrega reingresada correctamente, queda en estado PENDIENTE"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró una entrega con el ID indicado"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La entrega no está en estado NO_RECIBIDA"
            )
    })
    @PostMapping("/{id}/reingresar")
    public EntregaDTO reingresar(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Integer id) {
        var entrega = entregaRepository.buscarPorId(id);
        entrega.reingresarADeposito();
        return new EntregaDTO(entrega.getIdEntrega(), entrega.getEstadoEntrega());
    }
}
