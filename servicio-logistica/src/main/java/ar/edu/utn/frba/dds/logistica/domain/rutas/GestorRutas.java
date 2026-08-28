package ar.edu.utn.frba.dds.logistica.domain.rutas;

import java.util.ArrayList;
import java.util.List;

public class GestorRutas {

    private final List<Ruta> rutas = new ArrayList<>();
    private EstrategiaPlanificacion estrategia = new PlanificacionPropia(); // default; el broker (Entrega 4) podrá cambiarla

    public void setEstrategia(EstrategiaPlanificacion estrategia) {
        this.estrategia = estrategia;
    }

    public List<Ruta> planificar(List<Entrega> entregasPendientes, List<Camion> camionesDisponibles) {
        int siguienteId = rutas.size() + 1;
        List<Ruta> nuevasRutas = estrategia.planificar(entregasPendientes, camionesDisponibles, siguienteId);
        rutas.addAll(nuevasRutas);
        return nuevasRutas;
    }

    public List<Ruta> getRutas() {
        return rutas;
    }

    public Ruta buscarPorId(Integer idRuta) {
        return rutas.stream()
                .filter(r -> r.getIdRuta().equals(idRuta))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No existe la ruta con id: " + idRuta));
    }

    public List<Entrega> donacionesNoEntregadas() {
        return rutas.stream()
                .flatMap(r -> r.getParadas().stream())
                .flatMap(p -> p.getEntregas().stream())
                .filter(e -> e.getEstadoEntrega() != EstadoEntrega.ENTREGADA)
                .toList();
    }
}