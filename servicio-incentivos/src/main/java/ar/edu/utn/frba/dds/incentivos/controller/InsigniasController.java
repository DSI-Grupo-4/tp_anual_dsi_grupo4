package ar.edu.utn.frba.dds.incentivos.controller;

import ar.edu.utn.frba.dds.incentivos.dto.InsigniaDTO;
import ar.edu.utn.frba.dds.incentivos.service.InsigniasService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incentivos")
public class InsigniasController {
    private final InsigniasService insigniasService;

    public InsigniasController(InsigniasService insigniasService) {
        this.insigniasService = insigniasService;
    }

    @GetMapping("/donantes/{id}/insignias")
    public List<InsigniaDTO> obtenerInsignias(@PathVariable Long id) {
        return insigniasService.obtenerInsignias(id);
    }
}
