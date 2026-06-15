package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.dto.NecesidadDTO;
import ar.edu.utn.frba.dds.donaciones.dto.NecesidadExtraordinariaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.NecesidadRecurrenteDTO;
import ar.edu.utn.frba.dds.donaciones.service.NecesidadService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/necesidades")
public class NecesidadController {
    private final NecesidadService necesidadService;

    public NecesidadController(NecesidadService necesidadService) {
        this.necesidadService = necesidadService;
    }

    @PostMapping("/recurrentes")
    public NecesidadDTO crearRecurrente(
            @RequestBody NecesidadRecurrenteDTO dto) {

        return necesidadService.crearRecurrente(dto);
    }

    @PostMapping("/extraordinarias")
    public NecesidadDTO crearExtraordinaria(
            @RequestBody NecesidadExtraordinariaDTO dto) {

        return necesidadService.crearExtraordinaria(dto);
    }

    @GetMapping
    public List<NecesidadDTO> obtenerTodas() {
        return necesidadService.obtenerTodas();
    }

    @GetMapping("/{id}")
    public NecesidadDTO obtenerPorId(
            @PathVariable Long id) {

        return necesidadService.obtenerPorId(id);
    }

    @PutMapping("/{id}/recurrentes")
    public NecesidadDTO actualizarRecurrente(
            @PathVariable Long id,
            @RequestBody NecesidadRecurrenteDTO dto) {

        return necesidadService.actualizarRecurrente(id, dto);
    }

    @PutMapping("/{id}/extraordinarias")
    public NecesidadDTO actualizarExtraordinaria(
            @PathVariable Long id,
            @RequestBody NecesidadExtraordinariaDTO dto) {

        return necesidadService.actualizarExtraordinaria(id, dto);
    }

    @DeleteMapping("/{id}")
    public void eliminar(
            @PathVariable Long id) {

        necesidadService.eliminar(id);
    }
}