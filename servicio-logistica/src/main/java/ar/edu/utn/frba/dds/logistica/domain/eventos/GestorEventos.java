package ar.edu.utn.frba.dds.logistica.domain.eventos;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;

import java.util.ArrayList;
import java.util.List;

public class GestorEventos {

    private final List<EventoLogistico> eventos = new ArrayList<>();

    public EventoLogistico crearEvento(TipoEvento tipo, Entrega entrega) {
        EventoLogistico evento = new EventoLogistico(tipo, entrega);
        eventos.add(evento);
        return evento;
    }

    // Donaciones consulta esto vía GET, sin que Logística la invoque a ella (según el diagrama)
    public List<EventoLogistico> getEventosNoPublicados() {
        return eventos.stream().filter(e -> !e.isPublicado()).toList();
    }

    public List<EventoLogistico> getEventos() {
        return eventos;
    }
}