package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.dto.RankingEntidadDTO;
import ar.edu.utn.frba.dds.donaciones.dto.SolicitudRankingDTO;
import ar.edu.utn.frba.dds.donaciones.service.AsignacionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/asignaciones")
public class AsignacionController {
    private final AsignacionService asignacionService;

    public AsignacionController(AsignacionService asignacionService) {
        this.asignacionService = asignacionService;
    }

    @PostMapping("/ranking")
    public List<RankingEntidadDTO> generarRanking(
            @RequestBody SolicitudRankingDTO dto) {

        return asignacionService.generarRanking(dto);
    }
}