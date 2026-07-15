package ar.edu.utn.frba.dds.logistica.domain.rutas;

import ar.edu.utn.frba.dds.logistica.domain.planificacion.EstadoPlanificacion;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class GestorRutas {

    private static GestorRutas instancia;

    private Integer idPlanificacion;
    private LocalDate fechaPlanificada;
    private List<Ruta> rutas;
    private List<Camion> camiones;

    // Constructor privado
    private GestorRutas() {}

    public static GestorRutas getInstancia() {
        if (instancia == null) {
            instancia = new GestorRutas();
        }
        return instancia;
    }

    public List<Entrega> donacionesNoEntregadas() {

        return rutas.stream()
                .flatMap(ruta -> ruta.getParadas().stream())
                .flatMap(parada -> parada.getEntregas().stream())
                .filter(entrega -> entrega.getEstadoEntrega() != EstadoEntrega.ENTREGADA)
                .toList();
    }
}
