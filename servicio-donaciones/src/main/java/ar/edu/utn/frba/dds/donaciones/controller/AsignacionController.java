package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.dto.ResultadoMatchmakingDTO;
import ar.edu.utn.frba.dds.donaciones.dto.SolicitudAsignacionDTO;
import ar.edu.utn.frba.dds.donaciones.service.AsignacionService;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/asignaciones")
public class AsignacionController {

    private final AsignacionService asignacionService;

    public AsignacionController(AsignacionService asignacionService) {
        this.asignacionService = asignacionService;
    }

    @Operation(
            summary = "Obtener entidades candidatas",
            description = "Ejecuta el proceso de matchmaking para obtener las entidades beneficiarias candidatas para una solicitud de asignación."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Candidatas obtenidas correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de la solicitud inválidos"
            )
    })
    @PostMapping("/candidatas")
    public ResultadoMatchmakingDTO obtenerCandidatas(
            @RequestBody SolicitudAsignacionDTO dto) {

        return asignacionService.obtenerCandidatas(dto);
    }
}