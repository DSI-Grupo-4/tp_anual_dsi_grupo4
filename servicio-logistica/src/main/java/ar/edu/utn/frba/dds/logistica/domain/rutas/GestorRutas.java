package ar.edu.utn.frba.dds.logistica.domain.rutas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class GestorRutas {

    private final List<Ruta> rutas = new ArrayList<>();
    private EstrategiaPlanificacion estrategia = new PlanificacionPropia(); // default; el broker podrá cambiarla

    public void setEstrategia(EstrategiaPlanificacion estrategia) {
        this.estrategia = estrategia;
    }

    public List<Ruta> planificar(List<Entrega> entregasPendientes, List<Camion> camionesDisponibles,
                                  List<Chofer> choferesDisponibles) {
        int siguienteId = rutas.size() + 1;
        List<Ruta> nuevasRutas = estrategia.planificar(entregasPendientes, camionesDisponibles, choferesDisponibles, siguienteId);
        rutas.addAll(nuevasRutas);
        return nuevasRutas;
    }

    /**
     * Registra una ruta que vino armada de afuera (callback de un
     * componente externo de planificación, ver PlanificadorService) en vez
     * de calcularla con la estrategia in-process -- mismo id autoincremental
     * y misma lista real que planificar(), para que buscarPorId/getRutas
     * no tengan que distinguir el origen.
     */
    public Ruta registrarRutaExterna(Camion camion, Chofer chofer, List<Parada> paradas) {
        Ruta ruta = new Ruta(rutas.size() + 1, camion, chofer, LocalDate.now().plusDays(1), paradas);
        rutas.add(ruta);
        return ruta;
    }

    public List<Ruta> getRutas() {
        return rutas;
    }

    public Ruta buscarPorId(Integer idRuta) {
        return rutas.stream()
                .filter(r -> r.getIdRuta().equals(idRuta))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No existe la ruta con id: " + idRuta));
    }

    public List<Entrega> donacionesNoEntregadas() {
        return rutas.stream()
                .flatMap(r -> r.getParadas().stream())
                .flatMap(p -> p.getEntregas().stream())
                .filter(e -> e.getEstadoEntrega() != EstadoEntrega.ENTREGADA)
                .toList();
    }
}
