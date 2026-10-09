package ar.edu.utn.frba.dds.logistica.service;

import ar.edu.utn.frba.dds.logistica.domain.eventos.GestorEventos;
import ar.edu.utn.frba.dds.logistica.domain.eventos.TipoEvento;
import ar.edu.utn.frba.dds.logistica.domain.rutas.*;
import ar.edu.utn.frba.dds.logistica.dto.ParadaPlanificadaDTO;
import ar.edu.utn.frba.dds.logistica.dto.PlanExternoDTO;
import ar.edu.utn.frba.dds.logistica.dto.RutaPlanificadaDTO;
import ar.edu.utn.frba.dds.logistica.repository.EntregaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class PlanificadorService {

    private static final Set<EstadoEntrega> ASIGNABLES = Set.of(EstadoEntrega.PENDIENTE, EstadoEntrega.REPLANIFICABLE);

    private final EntregaRepository entregaRepository;
    private final CamionService camionService;
    private final ChoferService choferService;
    private final GestorRutas gestorRutas;
    private final GestorEventos gestorEventos;

    public PlanificadorService(EntregaRepository entregaRepository,
                               CamionService camionService,
                               ChoferService choferService,
                               GestorRutas gestorRutas,
                               GestorEventos gestorEventos) {
        this.entregaRepository = entregaRepository;
        this.camionService = camionService;
        this.choferService = choferService;
        this.gestorRutas = gestorRutas;
        this.gestorEventos = gestorEventos;
    }

    // Disparado por el scheduler (horario de baja carga) o manualmente vía endpoint
    public List<Ruta> planificarRutasDelDia() {
        List<Entrega> pendientes = entregaRepository.obtenerPendientes();
        if (pendientes.isEmpty()) return List.of();

        List<Camion> disponibles = camionService.obtenerCamionesDisponiblesEntidad();
        List<Chofer> choferesDisponibles = choferService.obtenerChoferesHabilitados();

        List<Ruta> rutasGeneradas = gestorRutas.planificar(pendientes, disponibles, choferesDisponibles);
        emitirRutaPlanificada(rutasGeneradas);
        return rutasGeneradas;
    }

    /**
     * URL de callback pedida por el enunciado: un componente externo de
     * planificación nos devuelve, por esta vía, a qué camión le tocó qué
     * entregas (agrupadas en paradas) -- en vez de calcularlo in-process
     * como hace PlanificacionPropia. Reutiliza exactamente el mismo cierre
     * (registrar en GestorRutas + comprometer el camión vía Ruta/Entrega +
     * emitir RUTA_PLANIFICADA) que el camino interno, para que a Donaciones
     * no le importe de dónde salió el plan.
     */
    public List<Ruta> registrarPlanExterno(PlanExternoDTO plan) {
        List<Ruta> rutasRegistradas = new ArrayList<>();

        for (RutaPlanificadaDTO rutaDTO : plan.getRutas()) {
            Camion camion = camionService.buscar(rutaDTO.getIdCamion());
            Chofer chofer = rutaDTO.getIdChofer() != null ? choferService.buscar(rutaDTO.getIdChofer()) : null;

            List<Parada> paradas = new ArrayList<>();
            int idParada = 1;
            for (ParadaPlanificadaDTO paradaDTO : rutaDTO.getParadas()) {
                List<Entrega> entregas = paradaDTO.getEntregasIds().stream()
                        .map(entregaRepository::buscarPorId)
                        .toList();
                for (Entrega entrega : entregas) {
                    if (!ASIGNABLES.contains(entrega.getEstadoEntrega())) {
                        throw new IllegalStateException(
                                "La entrega " + entrega.getIdEntrega() + " no está disponible para planificar (estado "
                                        + entrega.getEstadoEntrega() + ")");
                    }
                }
                Entrega primera = entregas.get(0);
                Parada parada = new Parada(idParada++, primera.getIdEntidadBeneficiariaAsociada(),
                        primera.getDireccionDestino(), entregas);
                entregas.forEach(e -> e.asignarARuta(camion));
                paradas.add(parada);
            }

            rutasRegistradas.add(gestorRutas.registrarRutaExterna(camion, chofer, paradas));
        }

        emitirRutaPlanificada(rutasRegistradas);
        return rutasRegistradas;
    }

    // emitimos evento por cada ruta planificada, para que Donaciones lo consuma vía GET
    private void emitirRutaPlanificada(List<Ruta> rutas) {
        rutas.forEach(ruta ->
                ruta.getParadas().forEach(parada ->
                        parada.getEntregas().forEach(entrega ->
                                gestorEventos.crearEvento(TipoEvento.RUTA_PLANIFICADA, entrega))));
    }
}
