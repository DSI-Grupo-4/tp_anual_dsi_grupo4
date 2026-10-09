package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.dto.CamionDTO;
import ar.edu.utn.frba.dds.logistica.service.CamionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/camiones")
public class CamionController {

    private final CamionService camionService;

    public CamionController(CamionService camionService) {
        this.camionService = camionService;
    }

    @Operation(
            summary = "Dar de alta un camión",
            description = "Registra un camión nuevo en la flota (patente, capacidad en volumen y kg, altura). Arranca en DISPONIBLE salvo que se indique otro estado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Camión creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Los datos del camión son inválidos")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CamionDTO crear(@Valid @RequestBody CamionDTO dto) {
        return camionService.crear(dto);
    }

    @Operation(
            summary = "Obtener todos los camiones",
            description = "Obtiene la lista de todos los camiones registrados en el sistema."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Camiones obtenidos correctamente"
            )
    })
    @GetMapping
    public List<CamionDTO> obtenerCamiones() {
        return camionService.obtenerCamiones();
    }

    @Operation(
            summary = "Obtener camiones disponibles",
            description = "Obtiene la lista de camiones que se encuentran actualmente disponibles para realizar entregas."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Camiones disponibles obtenidos correctamente"
            )
    })
    @GetMapping("/disponibles")
    public List<CamionDTO> obtenerCamionesDisponibles() {
        return camionService.obtenerCamionesDisponibles();
    }

    @Operation(
            summary = "Actualizar un camión",
            description = "Reemplaza los datos del camión (incluido su estado, ej. para marcarlo FUERA_DE_SERVICIO manualmente)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Camión actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe un camión con el ID indicado"),
            @ApiResponse(responseCode = "400", description = "Los datos del camión son inválidos")
    })
    @PutMapping("/{id}")
    public CamionDTO actualizar(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Integer id,
                                 @Valid @RequestBody CamionDTO dto) {
        return camionService.actualizar(id, dto);
    }

    @Operation(
            summary = "Eliminar un camión",
            description = "Da de baja un camión. Rechaza la baja si tiene entregas activas (asignada a ruta o en traslado)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Camión eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe un camión con el ID indicado"),
            @ApiResponse(responseCode = "409", description = "El camión tiene entregas activas asignadas")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Integer id) {
        camionService.eliminar(id);
    }
}
