package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.dto.CamionDTO;
import ar.edu.utn.frba.dds.logistica.service.CamionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/camiones")
public class CamionController {

    private final CamionService camionService;

    public CamionController(CamionService camionService) {
        this.camionService = camionService;
    }

    @Operation(
            summary = "Obtener todos los camiones",
            description = "Obtiene la lista de todos los camiones registrados en el sistema."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Camiones obtenidos correctamente"
            )
    })
    @GetMapping
    public List<CamionDTO> obtenerCamiones() {
        return camionService.obtenerCamiones();
    }

    @Operation(
            summary = "Obtener camiones disponibles",
            description = "Obtiene la lista de camiones que se encuentran actualmente disponibles para realizar entregas."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Camiones disponibles obtenidos correctamente"
            )
    })
    @GetMapping("/disponibles")
    public List<CamionDTO> obtenerCamionesDisponibles() {
        return camionService.obtenerCamionesDisponibles();
    }
}
