package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.dto.NecesidadDTO;
import ar.edu.utn.frba.dds.donaciones.dto.NecesidadExtraordinariaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.NecesidadRecurrenteDTO;
import ar.edu.utn.frba.dds.donaciones.service.NecesidadService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entidades/{entidadId}/necesidades")
public class NecesidadController {

    private final NecesidadService necesidadService;

    public NecesidadController(NecesidadService necesidadService) {
        this.necesidadService = necesidadService;
    }

    @GetMapping
    public List<NecesidadDTO> obtenerPorEntidad(@PathVariable Long entidadId) {
        return necesidadService.obtenerPorEntidad(entidadId);
    }

    @PostMapping("/recurrentes")
    @ResponseStatus(HttpStatus.CREATED)
    public NecesidadDTO crearRecurrente(
            @PathVariable Long entidadId,
            @RequestBody NecesidadRecurrenteDTO dto) {
        dto.setEntidadBeneficiariaId(entidadId);
        return necesidadService.crearRecurrente(dto);
    }

    @PostMapping("/extraordinarias")
    @ResponseStatus(HttpStatus.CREATED)
    public NecesidadDTO crearExtraordinaria(
            @PathVariable Long entidadId,
            @RequestBody NecesidadExtraordinariaDTO dto) {
        dto.setEntidadBeneficiariaId(entidadId);
        return necesidadService.crearExtraordinaria(dto);
    }

    @PutMapping("/{necesidadId}")
    public NecesidadDTO actualizar(
            @PathVariable Long entidadId,
            @PathVariable Long necesidadId,
            @RequestBody NecesidadDTO dto) {
        if ("RECURRENTE".equalsIgnoreCase(dto.getTipo())) {
            NecesidadRecurrenteDTO recDTO = new NecesidadRecurrenteDTO();
            recDTO.setDescripcion(dto.getDescripcion());
            recDTO.setSubcategoria(dto.getSubcategoria());
            recDTO.setCantidadRequerida(dto.getCantidadRequerida());
            recDTO.setPeriodicidad(dto.getPeriodicidad());
            recDTO.setEntidadBeneficiariaId(entidadId);
            return necesidadService.actualizarRecurrente(necesidadId, recDTO);
        }
        NecesidadExtraordinariaDTO extDTO = new NecesidadExtraordinariaDTO();
        extDTO.setDescripcion(dto.getDescripcion());
        extDTO.setSubcategoria(dto.getSubcategoria());
        extDTO.setCantidadRequerida(dto.getCantidadRequerida());
        extDTO.setTipoExtraordinario(dto.getTipoExtraordinario());
        extDTO.setEntidadBeneficiariaId(entidadId);
        return necesidadService.actualizarExtraordinaria(necesidadId, extDTO);
    }

    @DeleteMapping("/{necesidadId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(
            @PathVariable Long entidadId,
            @PathVariable Long necesidadId) {
        necesidadService.eliminar(necesidadId);
    }
}
