package ar.edu.utn.frba.dds.logistica.domain.eventos;


import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GestorEventos {
    private List <EventoLogistico> eventosDisponibles;

    public void crearEvento(){}
}
