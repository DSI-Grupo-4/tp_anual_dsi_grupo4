package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.dto.EntidadCandidataDTO;
import ar.edu.utn.frba.dds.donaciones.dto.SolicitudAsignacionDTO;
import ar.edu.utn.frba.dds.donaciones.service.AsignacionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asignaciones")
public class AsignacionController {

    private final AsignacionService asignacionService;

    public AsignacionController(AsignacionService asignacionService) {
        this.asignacionService = asignacionService;
    }

    @PostMapping("/candidatas")
    public List<EntidadCandidataDTO> obtenerCandidatas(
            @RequestBody SolicitudAsignacionDTO dto) {

        return asignacionService.obtenerCandidatas(dto);
    }
}