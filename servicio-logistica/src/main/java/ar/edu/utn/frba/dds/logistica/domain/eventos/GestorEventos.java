package ar.edu.utn.frba.dds.logistica.domain.eventos;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import ar.edu.utn.frba.dds.logistica.repository.EventoLogisticoRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Antes guardaba los eventos en una lista en memoria; ahora los persiste en
 * evento_logistico. Corre dentro de la transacción de quien lo invoca.
 */
public class GestorEventos {

    private final EventoLogisticoRepository eventoRepository;

    public GestorEventos(EventoLogisticoRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    public EventoLogistico crearEvento(TipoEvento tipo, Entrega entrega) {
        return eventoRepository.save(new EventoLogistico(tipo, entrega));
    }

    // Donaciones consulta esto vía GET, sin que Logística la invoque a ella (según el diagrama)
    public List<EventoLogistico> getEventosNoPublicados() {
        return eventoRepository.findByPublicadoFalseOrderByFechaGeneracionAsc();
    }

    public List<EventoLogistico> getEventos() {
        return eventoRepository.findAll();
    }

    public EventoLogistico buscarPorId(UUID idEvento) {
        return eventoRepository.findById(idEvento)
                .orElseThrow(() -> new NoSuchElementException("No existe el evento con id: " + idEvento));
    }
}
