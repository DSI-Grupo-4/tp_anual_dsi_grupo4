package ar.edu.utn.frba.dds.logistica.service;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Chofer;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoRuta;
import ar.edu.utn.frba.dds.logistica.dto.ChoferDTO;
import ar.edu.utn.frba.dds.logistica.repository.ChoferRepository;
import ar.edu.utn.frba.dds.logistica.repository.RutaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
@Transactional
public class ChoferService {

    private static final Set<EstadoRuta> RUTAS_ACTIVAS = Set.of(EstadoRuta.PLANIFICADA, EstadoRuta.INICIADA);

    private final ChoferRepository choferRepository;
    private final RutaRepository rutaRepository;

    public ChoferService(ChoferRepository choferRepository, RutaRepository rutaRepository) {
        this.choferRepository = choferRepository;
        this.rutaRepository = rutaRepository;
    }

    public ChoferDTO crear(ChoferDTO dto) {
        Chofer chofer = new Chofer(null, dto.getNombre(), dto.getDni(), dto.getHabilitado()); // el id lo asigna la base
        return toDTO(choferRepository.save(chofer));
    }

    @Transactional(readOnly = true)
    public List<ChoferDTO> obtenerChoferes() {
        return choferRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ChoferDTO> obtenerChoferesHabilitadosDTO() {
        return obtenerChoferesHabilitados().stream().map(this::toDTO).toList();
    }

    // usado internamente por el planificador
    @Transactional(readOnly = true)
    public List<Chofer> obtenerChoferesHabilitados() {
        return choferRepository.findByHabilitadoTrueOrderByIdChofer();
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
        long rutasActivas = rutaRepository.countByChoferAndEstadoRutaIn(chofer, RUTAS_ACTIVAS);
        if (rutasActivas > 0) {
            throw new IllegalStateException(
                    "No se puede eliminar el chofer " + id + ": tiene " + rutasActivas + " ruta(s) activa(s) asignada(s)");
        }
        // Con FK real, un chofer con historial no se puede borrar sin perder la trazabilidad.
        if (rutaRepository.existsByChofer(chofer)) {
            throw new IllegalStateException(
                    "No se puede eliminar el chofer " + id + ": figura en rutas anteriores. "
                            + "Marcarlo como no habilitado en su lugar.");
        }
        choferRepository.delete(chofer);
    }

    // usado por PlanificadorService.registrarPlanExterno para resolver el
    // chofer que indicó el proveedor externo en el callback
    @Transactional(readOnly = true)
    public Chofer buscar(Integer id) {
        return choferRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el chofer " + id));
    }

    private ChoferDTO toDTO(Chofer chofer) {
        return new ChoferDTO(chofer.getIdChofer(), chofer.getNombre(), chofer.getDni(), chofer.getHabilitado());
    }
}
