package ar.edu.utn.frba.dds.logistica.domain.rutas;

import ar.edu.utn.frba.dds.logistica.repository.CamionRepository;
import ar.edu.utn.frba.dds.logistica.repository.EntregaRepository;
import ar.edu.utn.frba.dds.logistica.repository.RutaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Las rutas se guardan en la colección "rutas" a través de RutaRepository.
 *
 * MongoDB no cascadea ni hace "dirty checking" como JPA: una Ruta referencia (DBRef) a su camión y a
 * las entregas de sus paradas, que son documentos de otras colecciones. Por eso guardar() persiste
 * explícitamente el camión, las entregas y la ruta. Las operaciones no son transaccionales entre
 * documentos (cada save es atómico por sí solo).
 */
public class GestorRutas {

    private final RutaRepository rutaRepository;
    private final EntregaRepository entregaRepository;
    private final CamionRepository camionRepository;
    private EstrategiaPlanificacion estrategia = new PlanificacionPropia(); // default; el broker podrá cambiarla

    public GestorRutas(RutaRepository rutaRepository, EntregaRepository entregaRepository,
                       CamionRepository camionRepository) {
        this.rutaRepository = rutaRepository;
        this.entregaRepository = entregaRepository;
        this.camionRepository = camionRepository;
    }

    public void setEstrategia(EstrategiaPlanificacion estrategia) {
        this.estrategia = estrategia;
    }

    public List<Ruta> planificar(List<Entrega> entregasPendientes, List<Camion> camionesDisponibles,
                                  List<Chofer> choferesDisponibles) {
        List<Ruta> nuevasRutas = estrategia.planificar(entregasPendientes, camionesDisponibles, choferesDisponibles);
        nuevasRutas.forEach(this::guardar);
        return nuevasRutas;
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
        return guardar(ruta);
    }

    /**
     * Persiste el agregado completo: primero el camión y las entregas (la ruta los referencia por id),
     * después la ruta con sus paradas embebidas.
     */
    public Ruta guardar(Ruta ruta) {
        camionRepository.save(ruta.getCamionAsociado());
        ruta.getParadas().forEach(parada -> entregaRepository.saveAll(parada.getEntregas()));
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
