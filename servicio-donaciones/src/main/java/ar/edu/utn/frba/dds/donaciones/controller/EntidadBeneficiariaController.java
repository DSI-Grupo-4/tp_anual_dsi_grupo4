package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.dto.EntidadBeneficiariaDTO;
import ar.edu.utn.frba.dds.donaciones.service.EntidadBeneficiariaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

@RestController
@RequestMapping("/api/entidades")
public class EntidadBeneficiariaController {

    private final EntidadBeneficiariaService entidadService;

    public EntidadBeneficiariaController(EntidadBeneficiariaService entidadService) {
        this.entidadService = entidadService;
    }

    @Operation(
            summary = "Obtener todas las entidades beneficiarias",
            description = "Obtiene la lista de todas las entidades beneficiarias registradas en el sistema."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Entidades beneficiarias obtenidas correctamente"
            )
    })
    @GetMapping
    public List<EntidadBeneficiariaDTO> obtenerTodas() {
        return entidadService.obtenerTodas();
    }

    @Operation(
            summary = "Obtener una entidad beneficiaria",
            description = "Obtiene una entidad beneficiaria a partir de su identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Entidad beneficiaria encontrada"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró una entidad beneficiaria con el ID indicado"
            )
    })
    @GetMapping("/{id}")
    public EntidadBeneficiariaDTO obtenerPorId(@PathVariable Long id) {
        return entidadService.obtenerPorId(id);
    }

    @Operation(
            summary = "Crear una entidad beneficiaria",
            description = "Registra una nueva entidad beneficiaria en el sistema."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Entidad beneficiaria creada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de la entidad beneficiaria inválidos"
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntidadBeneficiariaDTO crear(@RequestBody EntidadBeneficiariaDTO dto) {
        return entidadService.crear(dto);
    }

    @Operation(
            summary = "Actualizar una entidad beneficiaria",
            description = "Actualiza los datos de una entidad beneficiaria existente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Entidad beneficiaria actualizada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de la entidad beneficiaria inválidos"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Entidad beneficiaria no encontrada"
            )
    })
    @PutMapping("/{id}")
    public EntidadBeneficiariaDTO actualizar(
            @PathVariable Long id,
            @RequestBody EntidadBeneficiariaDTO dto) {
        return entidadService.actualizar(id, dto);
    }

    @Operation(
            summary = "Eliminar una entidad beneficiaria",
            description = "Elimina una entidad beneficiaria utilizando su identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Entidad beneficiaria eliminada correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Entidad beneficiaria no encontrada"
            )
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        entidadService.eliminar(id);
    }
}
