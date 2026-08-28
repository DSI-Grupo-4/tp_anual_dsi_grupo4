package ar.edu.utn.frba.dds.incentivos.controller;

import ar.edu.utn.frba.dds.incentivos.dto.MetricasDonanteDTO;
import ar.edu.utn.frba.dds.incentivos.service.MetricasService;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/incentivos")
public class MetricasController {
    private final MetricasService metricasService;

    public MetricasController(MetricasService metricasService) {
        this.metricasService = metricasService;
    }

    @Operation(
            summary = "Obtener métricas de un donante",
            description = "Obtiene las métricas y estadísticas asociadas a un donante."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Métricas obtenidas correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Donante no encontrado"
            )
    })
    @GetMapping("/donantes/{id}/metricas")
    public MetricasDonanteDTO obtenerMetricas(@PathVariable Long id) {
        return metricasService.obtenerMetricas(id);
    }
}
