package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.dto.ResultadoMatchmakingDTO;
import ar.edu.utn.frba.dds.donaciones.dto.SolicitudAsignacionDTO;
import ar.edu.utn.frba.dds.donaciones.service.AsignacionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Asignaciones", description = "Ejecución a demanda de los algoritmos de asignación sobre un ítem hipotético")
@RestController
@RequestMapping("/api/asignaciones")
public class AsignacionController {

    private final AsignacionService asignacionService;

    public AsignacionController(AsignacionService asignacionService) {
        this.asignacionService = asignacionService;
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Obtener candidatas", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @PostMapping("/candidatas")
    public ResultadoMatchmakingDTO obtenerCandidatas(
            @Valid @RequestBody SolicitudAsignacionDTO dto) {

        return asignacionService.obtenerCandidatas(dto);
    }
}
