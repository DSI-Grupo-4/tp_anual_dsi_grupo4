package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.domain.eventos.EventoLogistico;
import ar.edu.utn.frba.dds.logistica.domain.eventos.GestorEventos;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final GestorEventos gestorEventos;

    public EventoController(GestorEventos gestorEventos) {
        this.gestorEventos = gestorEventos;
    }

    // Donaciones consulta esto (pull), Logística nunca la invoca a ella
    @GetMapping
    public List<EventoLogistico> obtenerEventosNoPublicados() {
        return gestorEventos.getEventosNoPublicados();
    }

    @PostMapping("/{id}/marcar-publicado")
    public void marcarPublicado(@PathVariable java.util.UUID id) {
        gestorEventos.getEventos().stream()
                .filter(e -> e.getIdEvento().equals(id))
                .findFirst()
                .ifPresent(EventoLogistico::marcarPublicado);
    }
}
