package ar.edu.utn.frba.dds.logistica.domain.eventos;


import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class EventoLogistico {
    private UUID idEvento;
    private TipoEvento tipoEvento;
    private Boolean publicado;
    private Entrega entregaAsociada;

    public Entrega agregarEntrega(){
        return entregaAsociada;
    }

    public void marcarPublicado(){}
}
