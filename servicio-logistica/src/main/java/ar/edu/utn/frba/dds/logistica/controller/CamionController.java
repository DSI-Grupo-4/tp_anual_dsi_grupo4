package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.dto.CamionDTO;
import ar.edu.utn.frba.dds.logistica.service.CamionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/camiones")
public class CamionController {

    private final CamionService camionService;

    public CamionController(CamionService camionService) {
        this.camionService = camionService;
    }

    @GetMapping
    public List<CamionDTO> obtenerCamiones() {
        return camionService.obtenerCamiones();
    }

    @GetMapping("/disponibles")
    public List<CamionDTO> obtenerCamionesDisponibles() {
        return camionService.obtenerCamionesDisponibles();
    }
}
