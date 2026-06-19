package ar.edu.utn.frba.dds.incentivos.controller;

import ar.edu.utn.frba.dds.incentivos.dto.MisionDTO;
import ar.edu.utn.frba.dds.incentivos.service.MisionesService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incentivos")
public class MisionesController {
    private final MisionesService misionesService;

    public MisionesController(MisionesService misionesService) {
        this.misionesService = misionesService;
    }

    @GetMapping("/donantes/{id}/misiones")
    public List<MisionDTO> obtenerMisiones(@PathVariable Long id) {
        return misionesService.obtenerMisiones(id);
    }
}
