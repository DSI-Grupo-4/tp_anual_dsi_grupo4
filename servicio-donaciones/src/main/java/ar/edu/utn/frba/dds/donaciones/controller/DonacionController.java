package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.dto.DonacionDTO;
import ar.edu.utn.frba.dds.donaciones.service.DonacionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/donaciones")
public class DonacionController {

    private final DonacionService service;

    public DonacionController(DonacionService service) {
        this.service = service;
    }

    @PostMapping
    public DonacionDTO crear(@RequestBody DonacionDTO dto) {
        Donacion d = service.crear(dto);
        return service.toDTO(d);
    }

    @GetMapping
    public List<DonacionDTO> obtenerTodas() {
        return service.obtenerTodas()
                .stream()
                .map(service::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    public DonacionDTO obtenerPorId(@PathVariable Long id) {
        return service.toDTO(service.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    public void eliminar(
            @PathVariable Long id) {

        service.eliminar(id);
    }
}

