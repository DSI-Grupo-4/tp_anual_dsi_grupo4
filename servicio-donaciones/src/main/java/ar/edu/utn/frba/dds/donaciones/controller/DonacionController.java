package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.dto.CambioEstadoDTO;
import ar.edu.utn.frba.dds.donaciones.dto.DonacionDTO;
import ar.edu.utn.frba.dds.donaciones.dto.TimeStampDTO;
import ar.edu.utn.frba.dds.donaciones.service.DonacionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/donaciones")
public class DonacionController {
    private final DonacionService donacionService;

    public DonacionController(
            DonacionService donacionService) {

        this.donacionService = donacionService;
    }

    @PostMapping
    public DonacionDTO crear(
            @RequestBody DonacionDTO dto) {

        return donacionService.crear(dto);
    }

    @GetMapping
    public List<DonacionDTO> obtenerTodas() {

        return donacionService.obtenerTodas();
    }

    @GetMapping("/{id}")
    public DonacionDTO obtenerPorId(
            @PathVariable Long id) {

        return donacionService.obtenerPorId(id);
    }

    @DeleteMapping("/{id}")
    public void eliminar(
            @PathVariable Long id) {

        donacionService.eliminar(id);
    }

    @PatchMapping("/{id}/estado")
    public DonacionDTO cambiarEstado(
            @PathVariable Long id,
            @RequestBody CambioEstadoDTO dto) {

        return donacionService.cambiarEstado(id, dto);
    }

    @GetMapping("/{id}/historial")
    public List<TimeStampDTO> historial(
            @PathVariable Long id) {

        return donacionService.obtenerHistorial(id);
    }
}
