package ar.edu.utn.frba.dds.logistica.service;

import ar.edu.utn.frba.dds.logistica.domain.eventos.GestorEventos;
import ar.edu.utn.frba.dds.logistica.domain.eventos.TipoEvento;
import ar.edu.utn.frba.dds.logistica.domain.rutas.*;
import ar.edu.utn.frba.dds.logistica.repository.EntregaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanificadorService {

    private final EntregaRepository entregaRepository;
    private final CamionService camionService;
    private final GestorRutas gestorRutas;
    private final GestorEventos gestorEventos;

    public PlanificadorService(EntregaRepository entregaRepository,
                               CamionService camionService,
                               GestorRutas gestorRutas,
                               GestorEventos gestorEventos) {
        this.entregaRepository = entregaRepository;
        this.camionService = camionService;
        this.gestorRutas = gestorRutas;
        this.gestorEventos = gestorEventos;
    }

    // Disparado por el scheduler (horario de baja carga) o manualmente vía endpoint
    public List<Ruta> planificarRutasDelDia() {
        List<Entrega> pendientes = entregaRepository.obtenerPendientes();
        if (pendientes.isEmpty()) return List.of();

        List<Camion> disponibles = camionService.obtenerCamionesDisponiblesEntidad();

        List<Ruta> rutasGeneradas = gestorRutas.planificar(pendientes, disponibles);

        // emitimos evento por cada ruta planificada, para que Donaciones lo consuma vía GET
        rutasGeneradas.forEach(ruta ->
                ruta.getParadas().forEach(parada ->
                        parada.getEntregas().forEach(entrega ->
                                gestorEventos.crearEvento(TipoEvento.RUTA_PLANIFICADA, entrega))));

        return rutasGeneradas;
    }
}

