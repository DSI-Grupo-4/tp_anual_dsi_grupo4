package ar.edu.utn.frba.dds.logistica.service;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Camion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoCamion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoEntrega;
import ar.edu.utn.frba.dds.logistica.dto.CamionDTO;
import ar.edu.utn.frba.dds.logistica.repository.CamionRepository;
import ar.edu.utn.frba.dds.logistica.repository.EntregaRepository;
import ar.edu.utn.frba.dds.logistica.repository.RutaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
@Transactional
public class CamionService {

    // Estados en los que un camión está comprometido con una entrega real
    // -- no se puede borrar sin dejar una referencia colgante.
    private static final Set<EstadoEntrega> ENTREGAS_ACTIVAS = Set.of(
            EstadoEntrega.ASIGNADA_A_RUTA, EstadoEntrega.EN_TRASLADO);

    private final CamionRepository camionRepository;
    private final EntregaRepository entregaRepository;
    private final RutaRepository rutaRepository;

    public CamionService(CamionRepository camionRepository,
                         EntregaRepository entregaRepository,
                         RutaRepository rutaRepository) {
        this.camionRepository = camionRepository;
        this.entregaRepository = entregaRepository;
        this.rutaRepository = rutaRepository;
    }

    public CamionDTO crear(CamionDTO dto) {
        Camion camion = new Camion(
                null, // lo asigna la base
                dto.getPatente(),
                dto.getCapacidadVolumenM3(),
                dto.getAlturaM(),
                dto.getCapacidadCargaKg(),
                dto.getEstadoCamion() != null ? dto.getEstadoCamion() : EstadoCamion.DISPONIBLE);
        return toDTO(camionRepository.save(camion));
    }

    @Transactional(readOnly = true)
    public List<CamionDTO> obtenerCamiones() {
        return camionRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<CamionDTO> obtenerCamionesDisponibles() {
        return obtenerCamionesDisponiblesEntidad().stream().map(this::toDTO).toList();
    }

    // usado internamente por el planificador, que necesita las entidades de dominio, no DTOs
    @Transactional(readOnly = true)
    public List<Camion> obtenerCamionesDisponiblesEntidad() {
        return camionRepository.findByEstadoCamionOrderByIdCamion(EstadoCamion.DISPONIBLE);
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
        long entregasActivas = entregaRepository.countByCamionEntregaAndEstadoEntregaIn(camion, ENTREGAS_ACTIVAS);
        if (entregasActivas > 0) {
            throw new IllegalStateException(
                    "No se puede eliminar el camión " + id + ": tiene " + entregasActivas + " entrega(s) activa(s) asignada(s)");
        }
        // Con FK real, un camión con historial no se puede borrar sin perder la trazabilidad.
        if (rutaRepository.existsByCamionAsociado(camion) || entregaRepository.existsByCamionEntrega(camion)) {
            throw new IllegalStateException(
                    "No se puede eliminar el camión " + id + ": figura en rutas o entregas anteriores. "
                            + "Cambiarlo a FUERA_DE_SERVICIO en su lugar.");
        }
        camionRepository.delete(camion);
    }

    // usado por PlanificadorService.registrarPlanExterno para resolver el
    // camión que indicó el proveedor externo en el callback
    @Transactional(readOnly = true)
    public Camion buscar(Integer id) {
        return camionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el camión " + id));
    }

    private CamionDTO toDTO(Camion camion) {
        return new CamionDTO(camion.getIdCamion(), camion.getPatente(), camion.getCapacidadVolumenM3(),
                camion.getAlturaM(), camion.getCapacidadCargaKg(), camion.getEstadoCamion());
    }
}
