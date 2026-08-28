package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.domain.eventos.GestorEventos;
import ar.edu.utn.frba.dds.logistica.domain.eventos.TipoEvento;
import ar.edu.utn.frba.dds.logistica.domain.rutas.*;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/rutas/{idRuta}/paradas/{idParada}")
public class ParadaController {

    private final GestorRutas gestorRutas;
    private final GestorEventos gestorEventos;

    public ParadaController(GestorRutas gestorRutas, GestorEventos gestorEventos) {
        this.gestorRutas = gestorRutas;
        this.gestorEventos = gestorEventos;
    }

    @Operation(
            summary = "Confirmar recepción de una parada",
            description = "Confirma la recepción de las entregas asociadas a una parada y genera los eventos correspondientes."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Recepción confirmada correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró la ruta o la parada indicada"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Los datos de la recepción son inválidos"
            )
    })
    @PostMapping("/confirmar")
    public void confirmarRecepcion(@PathVariable Integer idRuta, @PathVariable Integer idParada,
                                   @RequestBody FotoEntrega foto) {
        Parada parada = buscarParada(idRuta, idParada);
        parada.confirmarRecepcion(foto);
        parada.getEntregas().forEach(e -> gestorEventos.crearEvento(TipoEvento.ENTREGA_CONFIRMADA, e));
    }

    @Operation(
            summary = "Marcar una parada como no recibida",
            description = "Registra que las entregas asociadas a una parada no fueron recibidas, indicando una justificación, y genera los eventos correspondientes."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Parada marcada como no recibida correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró la ruta o la parada indicada"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La justificación proporcionada no es válida"
            )
    })
    @PostMapping("/no-recibida")
    public void marcarNoRecibida(@PathVariable Integer idRuta, @PathVariable Integer idParada,
                                 @RequestBody String justificacion) {
        Parada parada = buscarParada(idRuta, idParada);
        parada.marcarNoRecibida(justificacion);
        parada.getEntregas().forEach(e -> gestorEventos.crearEvento(TipoEvento.ENTREGA_NO_RECIBIDA, e));
    }

    private Parada buscarParada(Integer idRuta, Integer idParada) {
        Ruta ruta = gestorRutas.buscarPorId(idRuta);
        return ruta.getParadas().stream()
                .filter(p -> p.getIdParada().equals(idParada))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No existe la parada con id: " + idParada));
    }
}
