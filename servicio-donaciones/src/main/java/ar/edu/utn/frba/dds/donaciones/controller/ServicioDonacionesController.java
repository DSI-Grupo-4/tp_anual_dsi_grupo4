package ar.edu.utn.frba.dds.donaciones.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
public class ServicioDonacionesController {

    @Operation(
            summary = "Verificar disponibilidad del servicio",
            description = "Comprueba que el microservicio de Donaciones se encuentra disponible y funcionando correctamente."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Servicio disponible"
            )
    })
    @GetMapping("/ping")
    public String ping() {
        return "Servicio Donaciones OK";
    }
}