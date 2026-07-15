package ar.edu.utn.frba.dds.logistica.domain.rutas;

import ar.edu.utn.frba.dds.logistica.domain.planificacion.Lote;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Parada {
    private Integer idEntidadBeneficiariaAsociada;
    private List<Entrega> entregas;

    public void confirmarRecepcion() {
        entregas.forEach(entrega -> entrega.cambiarEstado(EstadoEntrega.ENTREGADA));
    }
}
