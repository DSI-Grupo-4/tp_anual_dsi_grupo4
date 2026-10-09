package ar.edu.utn.frba.dds.logistica.service;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Camion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoCamion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoEntrega;
import ar.edu.utn.frba.dds.logistica.dto.CamionDTO;
import ar.edu.utn.frba.dds.logistica.repository.EntregaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CamionService {

    // Estados en los que un camión está comprometido con una entrega real
    // -- no se puede borrar sin dejar una referencia colgante.
    private static final Set<EstadoEntrega> ENTREGAS_ACTIVAS = Set.of(
            EstadoEntrega.ASIGNADA_A_RUTA, EstadoEntrega.EN_TRASLADO);

    // In-memory por ahora; esto se reemplazará por un repositorio con persistencia real.
    private final List<Camion> camiones = new ArrayList<>();
    private final AtomicInteger siguienteId = new AtomicInteger(1);
    private final EntregaRepository entregaRepository;

    public CamionService(EntregaRepository entregaRepository) {
        this.entregaRepository = entregaRepository;
    }

    public CamionDTO crear(CamionDTO dto) {
        Camion camion = new Camion(
                siguienteId.getAndIncrement(),
                dto.getPatente(),
                dto.getCapacidadVolumenM3(),
                dto.getAlturaM(),
                dto.getCapacidadCargaKg(),
                dto.getEstadoCamion() != null ? dto.getEstadoCamion() : EstadoCamion.DISPONIBLE);
        camiones.add(camion);
        return toDTO(camion);
    }

    public List<CamionDTO> obtenerCamiones() {
        return camiones.stream().map(this::toDTO).toList();
    }

    public List<CamionDTO> obtenerCamionesDisponibles() {
        return camiones.stream()
                .filter(c -> c.getEstadoCamion() == EstadoCamion.DISPONIBLE)
                .map(this::toDTO)
                .toList();
    }

    // usado internamente por el planificador, que necesita las entidades de dominio, no DTOs
    public List<Camion> obtenerCamionesDisponiblesEntidad() {
        return camiones.stream()
                .filter(c -> c.getEstadoCamion() == EstadoCamion.DISPONIBLE)
                .toList();
    }

    public CamionDTO actualizar(Integer id, CamionDTO dto) {
        Camion camion = buscar(id);
        camion.setPatente(dto.getPatente());
        camion.setCapacidadVolumenM3(dto.getCapacidadVolumenM3());
        camion.setAlturaM(dto.getAlturaM());
        camion.setCapacidadCargaKg(dto.getCapacidadCargaKg());
        if (dto.getEstadoCamion() != null) {
            camion.cambiarEstado(dto.getEstadoCamion());
        }
        return toDTO(camion);
    }

    public void eliminar(Integer id) {
        Camion camion = buscar(id);
        long entregasActivas = entregaRepository.obtenerTodas().stream()
                .filter(e -> camion.equals(e.getCamionEntrega()))
                .filter(e -> ENTREGAS_ACTIVAS.contains(e.getEstadoEntrega()))
                .count();
        if (entregasActivas > 0) {
            throw new IllegalStateException(
                    "No se puede eliminar el camión " + id + ": tiene " + entregasActivas + " entrega(s) activa(s) asignada(s)");
        }
        camiones.remove(camion);
    }

    // usado por PlanificadorService.registrarPlanExterno para resolver el
    // camión que indicó el proveedor externo en el callback
    public Camion buscar(Integer id) {
        return camiones.stream()
                .filter(c -> c.getIdCamion().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No existe el camión " + id));
    }

    private CamionDTO toDTO(Camion camion) {
        return new CamionDTO(camion.getIdCamion(), camion.getPatente(), camion.getCapacidadVolumenM3(),
                camion.getAlturaM(), camion.getCapacidadCargaKg(), camion.getEstadoCamion());
    }
}
