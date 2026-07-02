package ar.edu.utn.frba.dds.logistica.domain.planificacion;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Lote {
    private List <Entrega> entregas;

    public void agregarEntrega(Entrega entrega){}
}
