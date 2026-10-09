package ar.edu.utn.frba.dds.logistica.domain.rutas;

import ar.edu.utn.frba.dds.logistica.repository.RutaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Antes mantenía las rutas en una lista en memoria; ahora las guarda en la base
 * a través de RutaRepository. No es transaccional por sí mismo: corre dentro de
 * la transacción del service/controller que lo invoca.
 */
public class GestorRutas {

    private final RutaRepository rutaRepository;
    private EstrategiaPlanificacion estrategia = new PlanificacionPropia(); // default; el broker podrá cambiarla

    public GestorRutas(RutaRepository rutaRepository) {
        this.rutaRepository = rutaRepository;
    }

    public void setEstrategia(EstrategiaPlanificacion estrategia) {
        this.estrategia = estrategia;
    }

    public List<Ruta> planificar(List<Entrega> entregasPendientes, List<Camion> camionesDisponibles,
                                  List<Chofer> choferesDisponibles) {
        List<Ruta> nuevasRutas = estrategia.planificar(entregasPendientes, camionesDisponibles, choferesDisponibles);
        return rutaRepository.saveAll(nuevasRutas);
    }

    /**
     * Registra una ruta que vino armada de afuera (callback de un
     * componente externo de planificación, ver PlanificadorService) en vez
     * de calcularla con la estrategia in-process -- mismo guardado que
     * planificar(), para que buscarPorId/getRutas no tengan que distinguir
     * el origen.
     */
    public Ruta registrarRutaExterna(Camion camion, Chofer chofer, List<Parada> paradas) {
        Ruta ruta = new Ruta(null, camion, chofer, LocalDate.now().plusDays(1), paradas);
        return rutaRepository.save(ruta);
    }

    public List<Ruta> getRutas() {
        return rutaRepository.findAll();
    }

    public Ruta buscarPorId(Integer idRuta) {
        return rutaRepository.findById(idRuta)
                .orElseThrow(() -> new NoSuchElementException("No existe la ruta con id: " + idRuta));
    }

    public List<Entrega> donacionesNoEntregadas() {
        return getRutas().stream()
                .flatMap(r -> r.getParadas().stream())
                .flatMap(p -> p.getEntregas().stream())
                .filter(e -> e.getEstadoEntrega() != EstadoEntrega.ENTREGADA)
                .toList();
    }
}
