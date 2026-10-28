package ar.edu.utn.frba.dds.logistica.repository;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Camion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoEntrega;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Antes era un Map en memoria. Se conservan los métodos que ya usaban los
 * services y controllers (guardar, buscarPorId, ...) como default methods,
 * para no tocar a quienes los invocan.
 */
public interface EntregaRepository extends JpaRepository<Entrega, Integer> {

    Optional<Entrega> findByIdDonacionAsociada(Integer idDonacionAsociada);

    List<Entrega> findByEstadoEntregaInOrderByIdEntrega(Collection<EstadoEntrega> estados);

    List<Entrega> findByFechaOrderByIdEntrega(LocalDate fecha);

    long countByCamionEntregaAndEstadoEntregaIn(Camion camion, Collection<EstadoEntrega> estados);

    boolean existsByCamionEntrega(Camion camion);

    default Entrega guardar(Entrega entrega) {
        return save(entrega);
    }

    default Entrega buscarPorId(Integer id) {
        return findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe la entrega con id: " + id));
    }

    /**
     * Para que LoteService no cree una Entrega duplicada si Donaciones
     * reenvía la misma donación (ej. porque todavía figuraba "pendiente" de
     * su lado). Además, id_donacion_asociada es UNIQUE en la base.
     */
    default Optional<Entrega> buscarPorDonacion(Integer idDonacionAsociada) {
        return findByIdDonacionAsociada(idDonacionAsociada);
    }

    /**
     * Elegibles para la próxima planificación: las recién llegadas
     * (PENDIENTE) y las que una persona administradora revisó y aprobó
     * para reintentar (REPLANIFICABLE) -- ambas se tratan igual acá, la
     * diferencia es solo de trazabilidad (ver Entrega.reingresarADeposito).
     */
    default List<Entrega> obtenerPendientes() {
        return findByEstadoEntregaInOrderByIdEntrega(List.of(EstadoEntrega.PENDIENTE, EstadoEntrega.REPLANIFICABLE));
    }

    default List<Entrega> obtenerTodas() {
        return findAll();
    }

    default List<Entrega> obtenerPorFecha(LocalDate fecha) {
        return findByFechaOrderByIdEntrega(fecha);
    }
}
