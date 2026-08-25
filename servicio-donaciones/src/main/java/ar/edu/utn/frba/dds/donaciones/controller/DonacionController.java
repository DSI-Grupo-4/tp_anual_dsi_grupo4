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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

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

    @Operation(
            summary = "Crear una donación",
            description = "Registra una nueva donación en el sistema."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Donación creada correctamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de la donación inválidos"
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DonacionDTO crear(@RequestBody DonacionDTO dto) {
        return donacionService.crear(dto);
    }


    @Operation(
            summary = "Obtener todas las donaciones",
            description = "Obtiene todas las donaciones registradas en el sistema."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Donaciones obtenidas correctamente"
            )
    })
    @GetMapping
    public List<DonacionDTO> obtenerTodas() {
        return donacionService.obtenerTodas();
    }

    @Operation(
            summary = "Obtener donaciones pendientes",
            description = "Obtiene las donaciones que se encuentran pendientes de asignación."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Donaciones pendientes obtenidas correctamente"
            )
    })
    @GetMapping("/pendientes")
    public List<DonacionPendienteDTO> obtenerPendientes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return donacionService.obtenerPendientes(page, size);
    }

    @Operation(
            summary = "Obtener una donación",
            description = "Obtiene una donación a partir de su identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Donación encontrada"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró una donación con el ID indicado"
            )
    })
    @GetMapping("/{id}")
    public DonacionDTO obtenerPorId(@PathVariable Long id) {
        return donacionService.obtenerPorId(id);
    }

    @Operation(
            summary = "Actualizar una donación",
            description = "Actualiza los datos de una donación existente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Donación actualizada correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Donación no encontrada"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de la donación inválidos"
            )
    })
    @PutMapping("/{id}")
    public DonacionDTO actualizar(@PathVariable Long id, @RequestBody DonacionDTO dto) {
        dto.setId(id);
        return donacionService.crear(dto);
    }

    @Operation(
            summary = "Eliminar una donación",
            description = "Elimina una donación utilizando su identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Donación eliminada correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Donación no encontrada"
            )
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        donacionService.eliminar(id);
    }

    @Operation(
            summary = "Cambiar estado de una donación",
            description = "Modifica el estado actual de una donación."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado modificado correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Donación no encontrada"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Estado inválido"
            )
    })
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

    @Operation(
            summary = "Obtener entidades beneficiarias candidatas",
            description = "Ejecuta el proceso de matchmaking para determinar las entidades beneficiarias candidatas para una donación."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Candidatas obtenidas correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Donación no encontrada"
            )
    })
    @GetMapping("/{id}/candidatas")
    public List<EntidadBeneficiariaDTO> candidatas(@PathVariable Long id) {
        Donacion donacion = donacionService.obtenerDominioPorId(id);
        List<EntidadBeneficiaria> candidatas = matchmakingService.ejecutarMatchmaking(donacion);
        return candidatas.stream()
                .map(entidadBeneficiariaService::convertirADTO)
                .toList();
    }

    @Operation(
            summary = "Asignar una entidad beneficiaria",
            description = "Confirma la asignación de una entidad beneficiaria a una donación."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Entidad asignada correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Donación o entidad no encontrada"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La asignación no es válida"
            )
    })
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
