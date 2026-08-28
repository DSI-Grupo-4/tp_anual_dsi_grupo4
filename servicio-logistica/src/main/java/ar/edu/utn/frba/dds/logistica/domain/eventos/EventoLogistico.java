package ar.edu.utn.frba.dds.logistica.domain.eventos;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class EventoLogistico {
    private final UUID idEvento;
    private final TipoEvento tipoEvento;
    private boolean publicado;
    private final Entrega entregaAsociada;
    private final LocalDateTime fechaGeneracion;

    public EventoLogistico(TipoEvento tipoEvento, Entrega entregaAsociada) {
        this.idEvento = UUID.randomUUID();
        this.tipoEvento = tipoEvento;
        this.entregaAsociada = entregaAsociada;
        this.publicado = false;
        this.fechaGeneracion = LocalDateTime.now();
    }

    public void marcarPublicado() {
        this.publicado = true;
    }
}