package ar.edu.utn.frba.dds.incentivos.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frba.dds.incentivos.service.MetricasService;
import ar.edu.utn.frba.dds.incentivos.dto.MetricasDonanteDTO;

@RestController
@RequestMapping("/metricas")
public class MetricasController {

    private final MetricasService metricasService;

    public MetricasController(MetricasService metricasService) {
        this.metricasService = metricasService;
    }

    @GetMapping("/donantes/{id}")
    public MetricasDonanteDTO obtenerMetricas(@PathVariable Long id) {
        return metricasService.obtenerMetricas(id);
    }
}
