package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.dto.SimulacionAvanceDTO;
import ar.edu.utn.frba.dds.logistica.service.SimuladorRutaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Simulador de la demo: mueve el camión de una ruta en curso hacia su próxima parada. */
@RestController
@RequestMapping("/api/rutas/{idRuta}")
public class SimuladorController {

    private final SimuladorRutaService simulador;

    public SimuladorController(SimuladorRutaService simulador) {
        this.simulador = simulador;
    }

    @Operation(
            summary = "Simular un paso del recorrido de la ruta",
            description = "Avanza el camión una parte del camino hacia la próxima parada pendiente y guarda la nueva ubicación "
                    + "(marcada como simulada). No confirma entregas ni cambia estados. Solo para la demo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "El camión avanzó"),
            @ApiResponse(responseCode = "404", description = "No existe la ruta"),
            @ApiResponse(responseCode = "409", description = "La ruta no está en curso, no tiene paradas pendientes o la parada no tiene coordenadas")
    })
    @PostMapping("/simular-avance")
    public SimulacionAvanceDTO simularAvance(@PathVariable Integer idRuta) {
        return simulador.avanzar(idRuta);
    }
}
