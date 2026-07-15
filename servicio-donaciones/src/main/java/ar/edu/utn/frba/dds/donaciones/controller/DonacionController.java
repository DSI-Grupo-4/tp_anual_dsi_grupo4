package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.dto.AsignarEntidadDTO;
import ar.edu.utn.frba.dds.donaciones.dto.CambioEstadoDTO;
import ar.edu.utn.frba.dds.donaciones.dto.DonacionDTO;
import ar.edu.utn.frba.dds.donaciones.dto.DonacionPendienteDTO;
import ar.edu.utn.frba.dds.donaciones.dto.EntidadBeneficiariaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.TimeStampDTO;
import ar.edu.utn.frba.dds.donaciones.service.DonacionService;
import ar.edu.utn.frba.dds.donaciones.service.EntidadBeneficiariaService;
import ar.edu.utn.frba.dds.donaciones.service.MatchmakingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donaciones")
public class DonacionController {

    private final DonacionService donacionService;
    private final MatchmakingService matchmakingService;
    private final EntidadBeneficiariaService entidadBeneficiariaService;

    public DonacionController(
            DonacionService donacionService,
            MatchmakingService matchmakingService,
            EntidadBeneficiariaService entidadBeneficiariaService) {
        this.donacionService = donacionService;
        this.matchmakingService = matchmakingService;
        this.entidadBeneficiariaService = entidadBeneficiariaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DonacionDTO crear(@RequestBody DonacionDTO dto) {
        return donacionService.crear(dto);
    }

    @GetMapping
    public List<DonacionDTO> obtenerTodas() {
        return donacionService.obtenerTodas();
    }

    @GetMapping("/pendientes")
    public List<DonacionPendienteDTO> obtenerPendientes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return donacionService.obtenerPendientes(page, size);
    }

    @GetMapping("/{id}")
    public DonacionDTO obtenerPorId(@PathVariable Long id) {
        return donacionService.obtenerPorId(id);
    }

    @PutMapping("/{id}")
    public DonacionDTO actualizar(@PathVariable Long id, @RequestBody DonacionDTO dto) {
        dto.setId(id);
        return donacionService.crear(dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        donacionService.eliminar(id);
    }

    @PatchMapping("/{id}/estado")
    public DonacionDTO cambiarEstado(
            @PathVariable Long id,
            @RequestBody CambioEstadoDTO dto) {
        return donacionService.cambiarEstado(id, dto);
    }

    @GetMapping("/{id}/historial")
    public List<TimeStampDTO> historial(@PathVariable Long id) {
        return donacionService.obtenerHistorial(id);
    }

    @GetMapping("/{id}/candidatas")
    public List<EntidadBeneficiariaDTO> candidatas(@PathVariable Long id) {
        Donacion donacion = donacionService.obtenerDominioPorId(id);
        List<EntidadBeneficiaria> candidatas = matchmakingService.ejecutarMatchmaking(donacion);
        return candidatas.stream()
                .map(entidadBeneficiariaService::convertirADTO)
                .toList();
    }

    @PostMapping("/{id}/asignar")
    public DonacionDTO asignar(
            @PathVariable Long id,
            @RequestBody AsignarEntidadDTO dto) {
        Donacion donacion = donacionService.obtenerDominioPorId(id);
        EntidadBeneficiaria entidad =
                entidadBeneficiariaService.buscarEntidad(dto.getEntidadId());
        matchmakingService.confirmarAsignacion(donacion, entidad);
        return donacionService.obtenerPorId(id);
    }
}
