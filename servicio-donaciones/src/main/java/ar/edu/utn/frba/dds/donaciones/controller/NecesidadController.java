package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.dto.NecesidadDTO;
import ar.edu.utn.frba.dds.donaciones.dto.NecesidadExtraordinariaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.NecesidadRecurrenteDTO;
import ar.edu.utn.frba.dds.donaciones.service.NecesidadService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

@RestController
@RequestMapping("/api/entidades/{entidadId}/necesidades")
public class NecesidadController {

    private final NecesidadService necesidadService;

    public NecesidadController(NecesidadService necesidadService) {
        this.necesidadService = necesidadService;
    }

    @Operation(
            summary = "Obtener necesidades de una entidad",
            description = "Obtiene todas las necesidades asociadas a una entidad beneficiaria."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Necesidades obtenidas correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Entidad beneficiaria no encontrada"
            )
    })
    @GetMapping
    public List<NecesidadDTO> obtenerPorEntidad(@PathVariable Long entidadId) {
        return necesidadService.obtenerPorEntidad(entidadId);
    }

    @Operation(
            summary = "Crear una necesidad recurrente",
            description = "Registra una nueva necesidad recurrente asociada a una entidad beneficiaria."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Necesidad recurrente creada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de la necesidad inválidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Entidad beneficiaria no encontrada"
            )
    })
    @PostMapping("/recurrentes")
    @ResponseStatus(HttpStatus.CREATED)
    public NecesidadDTO crearRecurrente(
            @PathVariable Long entidadId,
            @RequestBody NecesidadRecurrenteDTO dto) {
        dto.setEntidadBeneficiariaId(entidadId);
        return necesidadService.crearRecurrente(dto);
    }

    @Operation(
            summary = "Crear una necesidad extraordinaria",
            description = "Registra una nueva necesidad extraordinaria asociada a una entidad beneficiaria."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Necesidad extraordinaria creada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de la necesidad inválidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Entidad beneficiaria no encontrada"
            )
    })
    @PostMapping("/extraordinarias")
    @ResponseStatus(HttpStatus.CREATED)
    public NecesidadDTO crearExtraordinaria(
            @PathVariable Long entidadId,
            @RequestBody NecesidadExtraordinariaDTO dto) {
        dto.setEntidadBeneficiariaId(entidadId);
        return necesidadService.crearExtraordinaria(dto);
    }

    @Operation(
            summary = "Actualizar una necesidad",
            description = "Actualiza una necesidad existente. El tipo de necesidad determina si se actualiza como recurrente o extraordinaria."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Necesidad actualizada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de la necesidad inválidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Entidad o necesidad no encontrada"
            )
    })
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

    @Operation(
            summary = "Eliminar una necesidad",
            description = "Elimina una necesidad asociada a una entidad beneficiaria."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Necesidad eliminada correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Entidad o necesidad no encontrada"
            )
    })
    @DeleteMapping("/{necesidadId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(
            @PathVariable Long entidadId,
            @PathVariable Long necesidadId) {
        necesidadService.eliminar(necesidadId);
    }
}
