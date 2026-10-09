package ar.edu.utn.frba.dds.logistica.service;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Chofer;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoRuta;
import ar.edu.utn.frba.dds.logistica.domain.rutas.GestorRutas;
import ar.edu.utn.frba.dds.logistica.dto.ChoferDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ChoferService {

    // In-memory por ahora; esto se reemplazará por un repositorio con persistencia real.
    private final List<Chofer> choferes = new ArrayList<>();
    private final AtomicInteger siguienteId = new AtomicInteger(1);
    private final GestorRutas gestorRutas;

    public ChoferService(GestorRutas gestorRutas) {
        this.gestorRutas = gestorRutas;
    }

    public ChoferDTO crear(ChoferDTO dto) {
        Chofer chofer = new Chofer(siguienteId.getAndIncrement(), dto.getNombre(), dto.getDni(), dto.getHabilitado());
        choferes.add(chofer);
        return toDTO(chofer);
    }

    public List<ChoferDTO> obtenerChoferes() {
        return choferes.stream().map(this::toDTO).toList();
    }

    public List<ChoferDTO> obtenerChoferesHabilitadosDTO() {
        return obtenerChoferesHabilitados().stream().map(this::toDTO).toList();
    }

    // usado internamente por el planificador
    public List<Chofer> obtenerChoferesHabilitados() {
        return choferes.stream().filter(Chofer::estaHabilitado).toList();
    }

    public ChoferDTO actualizar(Integer id, ChoferDTO dto) {
        Chofer chofer = buscar(id);
        chofer.setNombre(dto.getNombre());
        chofer.setDni(dto.getDni());
        chofer.setHabilitado(dto.getHabilitado());
        return toDTO(chofer);
    }

    public void eliminar(Integer id) {
        Chofer chofer = buscar(id);
        long rutasActivas = gestorRutas.getRutas().stream()
                .filter(r -> chofer.equals(r.getChofer()))
                .filter(r -> r.getEstadoRuta() == EstadoRuta.PLANIFICADA || r.getEstadoRuta() == EstadoRuta.INICIADA)
                .count();
        if (rutasActivas > 0) {
            throw new IllegalStateException(
                    "No se puede eliminar el chofer " + id + ": tiene " + rutasActivas + " ruta(s) activa(s) asignada(s)");
        }
        choferes.remove(chofer);
    }

    // usado por PlanificadorService.registrarPlanExterno para resolver el
    // chofer que indicó el proveedor externo en el callback
    public Chofer buscar(Integer id) {
        return choferes.stream()
                .filter(c -> c.getIdChofer().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No existe el chofer " + id));
    }

    private ChoferDTO toDTO(Chofer chofer) {
        return new ChoferDTO(chofer.getIdChofer(), chofer.getNombre(), chofer.getDni(), chofer.getHabilitado());
    }
}
