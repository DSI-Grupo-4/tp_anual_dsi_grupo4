package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.domain.personas.Donante;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaHumana;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaJuridica;
import ar.edu.utn.frba.dds.donaciones.dto.DonanteDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaHumanaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaJuridicaDTO;
import ar.edu.utn.frba.dds.donaciones.service.DonanteService;
import lombok.Getter;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/donantes")
public class DonanteController {
    private final DonanteService donanteService;

    public DonanteController(DonanteService donanteService) {
        this.donanteService = donanteService;
    }

    @PostMapping("/humanas")
    public DonanteDTO crearDonanteHumano(
            @RequestBody PersonaHumanaDTO dto) {
        return donanteService.crearDonanteHumano(dto);
    }

    @PostMapping("/juridicas")
    public DonanteDTO crearDonanteJuridico(
            @RequestBody PersonaJuridicaDTO dto) {
        return donanteService.crearDonanteJuridico(dto);
    }

    @GetMapping
    public List<DonanteDTO> obtenerTodos() {
        return donanteService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public DonanteDTO obtenerPorId(
            @PathVariable Long id) {
                return donanteService.buscarPorId(id);
    }

    @DeleteMapping("/{id}")
    public void eliminar(
            @PathVariable Long id) {
        donanteService.eliminar(id);
    }

    @PutMapping("/humanas/{id}")
    public DonanteDTO actualizarHumano(
            @PathVariable Long id,
            @RequestBody PersonaHumanaDTO dto) {

        return donanteService.actualizarHumano(id, dto);
    }

    @PutMapping("/juridicas/{id}")
    public DonanteDTO actualizarJuridico(
            @PathVariable Long id,
            @RequestBody PersonaJuridicaDTO dto) {

        return donanteService.actualizarJuridico(id, dto);
    }
}