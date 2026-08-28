package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.domain.eventos.GestorEventos;
import ar.edu.utn.frba.dds.logistica.domain.eventos.TipoEvento;
import ar.edu.utn.frba.dds.logistica.domain.rutas.GestorRutas;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Ruta;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

@RestController
@RequestMapping("/api/rutas")
public class RutaController {

    private final GestorRutas gestorRutas;
    private final GestorEventos gestorEventos;

    public RutaController(GestorRutas gestorRutas, GestorEventos gestorEventos) {
        this.gestorRutas = gestorRutas;
        this.gestorEventos = gestorEventos;
    }

    @Operation(
            summary = "Obtener todas las rutas",
            description = "Obtiene la lista de todas las rutas registradas en el sistema."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Rutas obtenidas correctamente"
            )
    })
    @GetMapping
    public List<Ruta> obtenerRutas() {
        return gestorRutas.getRutas();
    }

    @Operation(
            summary = "Obtener una ruta por ID",
            description = "Obtiene la información de una ruta a partir de su identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ruta encontrada correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró una ruta con el ID indicado"
            )
    })
    @GetMapping("/{id}")
    public Ruta obtenerRuta(@PathVariable Integer id) {
        return gestorRutas.buscarPorId(id);
    }

    @Operation(
            summary = "Iniciar una ruta",
            description = "Inicia el recorrido de una ruta e informa mediante eventos que las entregas asociadas a la ruta han comenzado su recorrido."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ruta iniciada correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró una ruta con el ID indicado"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La ruta no puede ser iniciada en su estado actual"
            )
    })
    // el chofer informa el comienzo de su recorrido
    @PostMapping("/{id}/iniciar")
    public Ruta iniciarRuta(@PathVariable Integer id) {
        Ruta ruta = gestorRutas.buscarPorId(id);
        ruta.iniciarRuta();
        ruta.getParadas().forEach(p -> p.getEntregas().forEach(e ->
                gestorEventos.crearEvento(TipoEvento.RUTA_INICIADA, e)));
        return ruta;
    }
}
