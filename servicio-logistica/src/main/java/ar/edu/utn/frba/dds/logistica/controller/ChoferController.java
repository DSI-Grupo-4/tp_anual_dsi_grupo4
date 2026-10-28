package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.dto.ChoferDTO;
import ar.edu.utn.frba.dds.logistica.service.ChoferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/choferes")
public class ChoferController {

    private final ChoferService choferService;

    public ChoferController(ChoferService choferService) {
        this.choferService = choferService;
    }

    @Operation(summary = "Dar de alta un chofer", description = "Registra un chofer nuevo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Chofer creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Los datos del chofer son inválidos")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChoferDTO crear(@Valid @RequestBody ChoferDTO dto) {
        return choferService.crear(dto);
    }

    @Operation(summary = "Obtener todos los choferes", description = "Lista todos los choferes registrados.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Choferes obtenidos correctamente")})
    @GetMapping
    public List<ChoferDTO> obtenerChoferes() {
        return choferService.obtenerChoferes();
    }

    @Operation(summary = "Obtener choferes habilitados", description = "Lista los choferes disponibles para ser asignados a una ruta.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Choferes habilitados obtenidos correctamente")})
    @GetMapping("/habilitados")
    public List<ChoferDTO> obtenerHabilitados() {
        return choferService.obtenerChoferesHabilitadosDTO();
    }

    @Operation(summary = "Actualizar un chofer", description = "Reemplaza los datos del chofer, incluida su habilitación.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chofer actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe un chofer con el ID indicado"),
            @ApiResponse(responseCode = "400", description = "Los datos del chofer son inválidos")
    })
    @PutMapping("/{id}")
    public ChoferDTO actualizar(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Integer id,
                                 @Valid @RequestBody ChoferDTO dto) {
        return choferService.actualizar(id, dto);
    }

    @Operation(summary = "Eliminar un chofer", description = "Da de baja un chofer. Rechaza la baja si tiene una ruta planificada o iniciada asignada.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Chofer eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe un chofer con el ID indicado"),
            @ApiResponse(responseCode = "409", description = "El chofer tiene una ruta activa asignada")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Integer id) {
        choferService.eliminar(id);
    }
}
