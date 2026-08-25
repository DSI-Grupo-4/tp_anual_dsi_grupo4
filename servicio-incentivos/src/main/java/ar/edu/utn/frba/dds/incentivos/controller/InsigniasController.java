package ar.edu.utn.frba.dds.incentivos.controller;

import ar.edu.utn.frba.dds.incentivos.dto.InsigniaDTO;
import ar.edu.utn.frba.dds.incentivos.service.InsigniasService;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

@RestController
@RequestMapping("/api/incentivos")
public class InsigniasController {
    private final InsigniasService insigniasService;

    public InsigniasController(InsigniasService insigniasService) {
        this.insigniasService = insigniasService;
    }

    @Operation(
            summary = "Obtener insignias de un donante",
            description = "Obtiene las insignias asociadas a un donante."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Insignias obtenidas correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Donante no encontrado"
            )
    })
    @GetMapping("/donantes/{id}/insignias")
    public List<InsigniaDTO> obtenerInsignias(@PathVariable Long id) {
        return insigniasService.obtenerInsignias(id);
    }
}
