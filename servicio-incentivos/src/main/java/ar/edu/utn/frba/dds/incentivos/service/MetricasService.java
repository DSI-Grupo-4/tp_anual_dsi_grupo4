package ar.edu.utn.frba.dds.incentivos.service;

import org.springframework.stereotype.Service;
import ar.edu.utn.frba.dds.incentivos.dto.MetricasDonanteDTO;

@Service
public class MetricasService {

    public MetricasDonanteDTO obtenerMetricas(Long id) {
        // mock por ahora
        return new MetricasDonanteDTO();
    }
}
