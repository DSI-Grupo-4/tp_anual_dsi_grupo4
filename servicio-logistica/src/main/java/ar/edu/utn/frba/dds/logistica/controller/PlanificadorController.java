package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Ruta;
import ar.edu.utn.frba.dds.logistica.dto.PlanExternoDTO;
import ar.edu.utn.frba.dds.logistica.service.PlanificadorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

@RestController
@RequestMapping("/api/planificador")
public class PlanificadorController {

    private final PlanificadorService planificadorService;

    public PlanificadorController(PlanificadorService planificadorService) {
        this.planificadorService = planificadorService;
    }

    @Operation(
            summary = "Ejecutar planificación de rutas",
            description = "Ejecuta manualmente la planificación de las rutas correspondientes al día. Esta operación normalmente es ejecutada automáticamente por el scheduler."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Planificación ejecutada correctamente"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error al ejecutar la planificación"
            )
    })
    // dispara manualmente lo que normalmente hace el scheduler de madrugada (útil para probar por Postman)
    @PostMapping("/ejecutar")
    public List<Ruta> ejecutarPlanificacion() {
        return planificadorService.planificarRutasDelDia();
    }

    @Operation(
            summary = "Callback de planificación externa",
            description = "URL de callback para que un componente externo de planificación de rutas notifique el resultado de una ejecución: a qué camión (y opcionalmente chofer) le tocó qué entregas, agrupadas en paradas. Los ids de entrega/camión/chofer deben existir ya en este servicio -- no crea entidades nuevas, solo registra la asignación y emite RUTA_PLANIFICADA."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plan registrado correctamente"),
            @ApiResponse(responseCode = "400", description = "El plan es inválido (estructura o ids inexistentes)"),
            @ApiResponse(responseCode = "409", description = "Alguna entrega indicada ya no está disponible para planificar")
    })
    @PostMapping("/callback")
    public List<Ruta> callback(@Valid @RequestBody PlanExternoDTO plan) {
        return planificadorService.registrarPlanExterno(plan);
    }
}
