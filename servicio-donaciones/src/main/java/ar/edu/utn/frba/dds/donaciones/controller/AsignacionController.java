package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.dto.ResultadoMatchmakingDTO;
import ar.edu.utn.frba.dds.donaciones.dto.SolicitudAsignacionDTO;
import ar.edu.utn.frba.dds.donaciones.service.AsignacionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/asignaciones")
public class AsignacionController {

    private final AsignacionService asignacionService;

    public AsignacionController(AsignacionService asignacionService) {
        this.asignacionService = asignacionService;
    }

    @PostMapping("/candidatas")
    public ResultadoMatchmakingDTO obtenerCandidatas(
            @RequestBody SolicitudAsignacionDTO dto) {

        return asignacionService.obtenerCandidatas(dto);
    }
}