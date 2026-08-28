package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import ar.edu.utn.frba.dds.logistica.dto.DonacionDTO;
import ar.edu.utn.frba.dds.logistica.service.LoteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@RestController
@RequestMapping("/api/lotes")
public class LoteController {

    private final LoteService loteService;

    public LoteController(LoteService loteService) {
        this.loteService = loteService;
    }

    @Operation(
            summary = "Recibir un lote de donaciones",
            description = "Recibe un lote de donaciones y genera las entregas correspondientes para su posterior planificación logística."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Lote recibido correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Entrega.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Los datos de las donaciones son inválidos"
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<Entrega> recibirLote(@RequestBody List<DonacionDTO> donaciones) {
        return loteService.recibirLote(donaciones);
    }
}