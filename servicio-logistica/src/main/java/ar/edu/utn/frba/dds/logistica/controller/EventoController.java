package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.domain.eventos.EventoLogistico;
import ar.edu.utn.frba.dds.logistica.domain.eventos.GestorEventos;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final GestorEventos gestorEventos;

    public EventoController(GestorEventos gestorEventos) {
        this.gestorEventos = gestorEventos;
    }

    @Operation(
            summary = "Obtener eventos no publicados",
            description = "Obtiene los eventos logísticos que todavía no fueron publicados. Este endpoint es consultado por otros servicios para obtener los eventos pendientes de publicación."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Eventos no publicados obtenidos correctamente"
            )
    })
    // Donaciones consulta esto (pull), Logística nunca la invoca a ella
    @GetMapping
    public List<EventoLogistico> obtenerEventosNoPublicados() {
        return gestorEventos.getEventosNoPublicados();
    }

    @Operation(
            summary = "Marcar un evento como publicado",
            description = "Marca un evento logístico como publicado a partir de su identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Evento marcado como publicado correctamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró un evento con el ID indicado"
            )
    })
    @PostMapping("/{id}/marcar-publicado")
    public void marcarPublicado(@PathVariable java.util.UUID id) {
        gestorEventos.getEventos().stream()
                .filter(e -> e.getIdEvento().equals(id))
                .findFirst()
                .ifPresent(EventoLogistico::marcarPublicado);
    }
}
