package ar.edu.utn.frba.dds.incentivos.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frba.dds.incentivos.service.MisionesService;
import ar.edu.utn.frba.dds.incentivos.dto.MisionDTO;

import java.util.List;

@RestController
@RequestMapping("/misiones")
public class MisionesController {

    private final MisionesService misionesService;

    public MisionesController(MisionesService misionesService) {
        this.misionesService = misionesService;
    }

    @GetMapping("/donantes/{id}")
    public List<MisionDTO> obtenerMisiones(@PathVariable Long id) {
        return misionesService.obtenerMisiones(id);
    }
}
