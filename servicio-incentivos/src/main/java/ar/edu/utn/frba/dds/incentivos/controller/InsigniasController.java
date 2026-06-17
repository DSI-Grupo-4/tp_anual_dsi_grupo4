package ar.edu.utn.frba.dds.incentivos.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.utn.frba.dds.incentivos.service.InsigniasService;
import ar.edu.utn.frba.dds.incentivos.dto.InsigniaDTO;

@RestController
@RequestMapping("/insignias")
public class InsigniasController {

    private final InsigniasService insigniasService;

    public InsigniasController(InsigniasService insigniasService) {
        this.insigniasService = insigniasService;
    }

    @GetMapping("/donantes/{id}")
    public List<InsigniaDTO> obtenerInsignias(@PathVariable Long id) {
        return insigniasService.obtenerInsignias(id);
    }
}
