package ar.edu.utn.frba.dds.incentivos.controller;

import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/incentivos")
public class ServicioIncentivosController {

    @Operation(
            summary = "Verificar disponibilidad del servicio",
            description = "Comprueba que el microservicio de Incentivos se encuentra disponible y funcionando correctamente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Servicio disponible"
            )
    })
    @GetMapping("/ping")
    public String ping() {
        return "incentivos OK";
    }
}
