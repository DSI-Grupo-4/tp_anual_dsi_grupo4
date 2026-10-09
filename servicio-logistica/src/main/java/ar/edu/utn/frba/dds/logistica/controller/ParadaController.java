package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.domain.eventos.GestorEventos;
import ar.edu.utn.frba.dds.logistica.domain.eventos.TipoEvento;
import ar.edu.utn.frba.dds.logistica.domain.rutas.*;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.NoSuchElementException;

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
    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/confirmar")
    public void confirmarRecepcion(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Integer idRuta, @io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Integer idParada,
                                   @jakarta.validation.Valid @RequestBody FotoEntrega foto) {
        Ruta ruta = gestorRutas.buscarPorId(idRuta);
        Parada parada = buscarParada(ruta, idParada);
        parada.confirmarRecepcion(foto);
        parada.getEntregas().forEach(e -> gestorEventos.crearEvento(TipoEvento.ENTREGA_CONFIRMADA, e));
        liberarCamionSiCorresponde(ruta);
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
    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/no-recibida")
    public void marcarNoRecibida(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Integer idRuta, @io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Integer idParada,
                                 @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "text/plain", examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = "Nadie respondió en el domicilio"))) @RequestBody String justificacion) {
        Ruta ruta = gestorRutas.buscarPorId(idRuta);
        Parada parada = buscarParada(ruta, idParada);
        parada.marcarNoRecibida(justificacion);
        parada.getEntregas().forEach(e -> gestorEventos.crearEvento(TipoEvento.ENTREGA_NO_RECIBIDA, e));
        liberarCamionSiCorresponde(ruta);
    }

    @Operation(
            summary = "Reportar un incidente logístico en una parada",
            description = "Para cuando la entrega no se pudo concretar por un motivo distinto a que la entidad no la haya recibido (vencimiento de los bienes antes de llegar, incidente logístico en el camino, etc.). Lo reporta el chofer o una persona administradora."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Incidente registrado correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró la ruta o la parada indicada"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El motivo proporcionado no es válido"
            )
    })
    @org.springframework.transaction.annotation.Transactional
    @PostMapping("/incidente")
    public void marcarFallida(@io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Integer idRuta, @io.swagger.v3.oas.annotations.Parameter(description = "Reemplazar por un ID existente devuelto por el alta o listado.", example = "1") @PathVariable Integer idParada,
                              @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @io.swagger.v3.oas.annotations.media.Content(mediaType = "text/plain", examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = "Los bienes vencieron antes de llegar a destino"))) @RequestBody String motivo) {
        Ruta ruta = gestorRutas.buscarPorId(idRuta);
        Parada parada = buscarParada(ruta, idParada);
        parada.marcarFallida(motivo);
        parada.getEntregas().forEach(e -> gestorEventos.crearEvento(TipoEvento.ENTREGA_FALLIDA, e));
        liberarCamionSiCorresponde(ruta);
    }

    private Parada buscarParada(Ruta ruta, Integer idParada) {
        return ruta.getParadas().stream()
                .filter(p -> p.getIdParada().equals(idParada))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No existe la parada con id: " + idParada));
    }

    // El camión queda ASIGNADO desde que se planifica su ruta (ver Ruta);
    // recién vuelve a estar DISPONIBLE cuando no queda nada pendiente de
    // resolver en ninguna de sus paradas -- antes finalizarRuta() nunca se
    // llamaba y el camión quedaba comprometido para siempre tras su primera ruta.
    private void liberarCamionSiCorresponde(Ruta ruta) {
        if (Boolean.TRUE.equals(ruta.completoTodasLasEntregas())) {
            ruta.finalizarRuta();
        }
    }
}
