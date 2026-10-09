package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.domain.eventos.GestorEventos;
import ar.edu.utn.frba.dds.logistica.domain.eventos.TipoEvento;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import ar.edu.utn.frba.dds.logistica.dto.EntregaDTO;
import ar.edu.utn.frba.dds.logistica.repository.EntregaRepository;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/entregas")
public class EntregaController {

    private final EntregaRepository entregaRepository;
    private final GestorEventos gestorEventos;

    public EntregaController(EntregaRepository entregaRepository, GestorEventos gestorEventos) {
        this.entregaRepository = entregaRepository;
        this.gestorEventos = gestorEventos;
    }

    @Operation(
            summary = "Obtener todas las entregas",
            description = "Lista las entregas registradas (lo que llegó vía POST /api/lotes), opcionalmente filtradas por fecha de recepción."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Entregas obtenidas correctamente"
            )
    })
    @GetMapping
    public List<Entrega> obtenerTodas(
            @io.swagger.v3.oas.annotations.Parameter(description = "Filtra por fecha de recepción del lote (yyyy-MM-dd). Opcional.", example = "2026-10-09")
            @RequestParam(required = false) LocalDate fecha) {
        return fecha != null ? entregaRepository.obtenerPorFecha(fecha) : entregaRepository.obtenerTodas();
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
            summary = "Reingresar una entrega NO_RECIBIDA o FALLIDA al depósito",
            description = "La persona administradora revisó el caso y determinó que se puede reintentar: la entrega pasa a REPLANIFICABLE, que la próxima planificación toma igual que una PENDIENTE."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Entrega reingresada correctamente, queda en estado REPLANIFICABLE"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró una entrega con el ID indicado"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La entrega no está en estado NO_RECIBIDA ni FALLIDA"
            )
    })
    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/{id}/reingresar")
    public EntregaDTO reingresar(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Integer id) {
        var entrega = entregaRepository.buscarPorId(id);
        entrega.reingresarADeposito();
        gestorEventos.crearEvento(TipoEvento.ENTREGA_REPLANIFICADA, entrega);
        return new EntregaDTO(entrega.getIdEntrega(), entrega.getEstadoEntrega());
    }
}
