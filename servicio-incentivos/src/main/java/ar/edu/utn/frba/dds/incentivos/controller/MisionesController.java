package ar.edu.utn.frba.dds.incentivos.controller;

import ar.edu.utn.frba.dds.incentivos.dto.MisionDTO;
import ar.edu.utn.frba.dds.incentivos.service.MisionesService;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

@RestController
@RequestMapping("/api/incentivos")
public class MisionesController {
    private final MisionesService misionesService;

    public MisionesController(MisionesService misionesService) {
        this.misionesService = misionesService;
    }

    @Operation(
            summary = "Obtener misiones de un donante",
            description = "Obtiene las misiones disponibles y asociadas a un donante."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Misiones obtenidas correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Donante no encontrado"
            )
    })
    @GetMapping("/donantes/{id}/misiones")
    public List<MisionDTO> obtenerMisiones(@PathVariable Long id) {
        return misionesService.obtenerMisiones(id);
    }
}
