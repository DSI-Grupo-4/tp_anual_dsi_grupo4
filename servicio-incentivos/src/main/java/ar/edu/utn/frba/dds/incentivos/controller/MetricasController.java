package ar.edu.utn.frba.dds.incentivos.controller;

import ar.edu.utn.frba.dds.incentivos.dto.MetricasDonanteDTO;
import ar.edu.utn.frba.dds.incentivos.service.MetricasService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/incentivos")
public class MetricasController {
    private final MetricasService metricasService;

    public MetricasController(MetricasService metricasService) {
        this.metricasService = metricasService;
    }

    @GetMapping("/donantes/{id}/metricas")
    public MetricasDonanteDTO obtenerMetricas(@PathVariable Long id) {
        return metricasService.obtenerMetricas(id);
    }
}
