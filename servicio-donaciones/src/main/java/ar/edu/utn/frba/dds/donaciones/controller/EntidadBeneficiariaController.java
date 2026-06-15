package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.dto.EntidadBeneficiariaDTO;
import ar.edu.utn.frba.dds.donaciones.service.EntidadBeneficiariaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/entidades")
public class EntidadBeneficiariaController {
    private final EntidadBeneficiariaService entidadService;

    public EntidadBeneficiariaController(
            EntidadBeneficiariaService entidadService) {
        this.entidadService = entidadService;
    }

    @PostMapping
    public EntidadBeneficiaria crear(@RequestBody EntidadBeneficiariaDTO dto) {
        return entidadService.crear(dto);
    }

    @GetMapping
    public List<EntidadBeneficiaria> obtenerTodas() {
        return entidadService.obtenerTodas();
    }

    @GetMapping("/{id}")
    public EntidadBeneficiaria obtenerPorId(
            @PathVariable Long id) {

        return entidadService.obtenerPorId(id);
    }

    @DeleteMapping("/{id}")
    public void eliminar(
            @PathVariable Long id) {

        entidadService.eliminar(id);
    }
}
