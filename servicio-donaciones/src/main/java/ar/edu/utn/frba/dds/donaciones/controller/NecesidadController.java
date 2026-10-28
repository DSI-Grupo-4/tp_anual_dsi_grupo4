package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.dto.NecesidadDTO;
import ar.edu.utn.frba.dds.donaciones.dto.NecesidadExtraordinariaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.NecesidadRecurrenteDTO;
import ar.edu.utn.frba.dds.donaciones.service.NecesidadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Necesidades", description = "Necesidades materiales (recurrentes y extraordinarias) de una entidad beneficiaria")
@RestController
@RequestMapping("/api/entidades/{entidadId}/necesidades")
public class NecesidadController {

    private final NecesidadService necesidadService;

    public NecesidadController(NecesidadService necesidadService) {
        this.necesidadService = necesidadService;
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Obtener por entidad", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @GetMapping
    public List<NecesidadDTO> obtenerPorEntidad(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Long entidadId) {
        return necesidadService.obtenerPorEntidad(entidadId);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Crear recurrente", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @PostMapping("/recurrentes")
    @ResponseStatus(HttpStatus.CREATED)
    public NecesidadDTO crearRecurrente(
            @io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Long entidadId,
            @Valid @RequestBody NecesidadRecurrenteDTO dto) {
        dto.setEntidadBeneficiariaId(entidadId);
        return necesidadService.crearRecurrente(dto);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Crear extraordinaria", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @PostMapping("/extraordinarias")
    @ResponseStatus(HttpStatus.CREATED)
    public NecesidadDTO crearExtraordinaria(
            @io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Long entidadId,
            @Valid @RequestBody NecesidadExtraordinariaDTO dto) {
        dto.setEntidadBeneficiariaId(entidadId);
        return necesidadService.crearExtraordinaria(dto);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Actualizar", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @PutMapping("/{necesidadId}")
    public NecesidadDTO actualizar(
            @io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Long entidadId,
            @io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Long necesidadId,
            @Valid @RequestBody NecesidadDTO dto) {
        if (!"RECURRENTE".equalsIgnoreCase(dto.getTipo()) && !"EXTRAORDINARIA".equalsIgnoreCase(dto.getTipo()))
            throw new IllegalArgumentException("tipo debe ser RECURRENTE o EXTRAORDINARIA");
        if ("RECURRENTE".equalsIgnoreCase(dto.getTipo())) {
            NecesidadRecurrenteDTO recDTO = new NecesidadRecurrenteDTO();
            recDTO.setDescripcion(dto.getDescripcion());
            recDTO.setSubcategoria(dto.getSubcategoria());
            recDTO.setUnidadMedida(dto.getUnidadMedida());
            recDTO.setCantidadRequerida(dto.getCantidadRequerida());
            recDTO.setPeriodicidad(dto.getPeriodicidad());
            recDTO.setEntidadBeneficiariaId(entidadId);
            return necesidadService.actualizarRecurrente(entidadId, necesidadId, recDTO);
        }
        NecesidadExtraordinariaDTO extDTO = new NecesidadExtraordinariaDTO();
        extDTO.setDescripcion(dto.getDescripcion());
        extDTO.setSubcategoria(dto.getSubcategoria());
        extDTO.setUnidadMedida(dto.getUnidadMedida());
        extDTO.setCantidadRequerida(dto.getCantidadRequerida());
        extDTO.setTipoExtraordinario(dto.getTipoExtraordinario());
        extDTO.setEntidadBeneficiariaId(entidadId);
        return necesidadService.actualizarExtraordinaria(entidadId, necesidadId, extDTO);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Eliminar", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @DeleteMapping("/{necesidadId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(
            @io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Long entidadId,
            @io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Long necesidadId) {
        necesidadService.eliminar(entidadId, necesidadId);
    }
}
