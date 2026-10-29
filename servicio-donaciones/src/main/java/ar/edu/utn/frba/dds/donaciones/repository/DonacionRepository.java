package ar.edu.utn.frba.dds.donaciones.repository;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonacionRepository extends JpaRepository<Donacion, Long> {

    List<Donacion> findByEstadoActual(EstadoTrack estado);

    // Reemplaza el filtro en memoria de DonacionService.obtenerPendientes():
    // misma condición que estaPendienteDePlanificacion (ASIGNACION_REALIZADA
    // + entidad asignada), pero paginado por la base, no por findAll()+skip/limit.
    List<Donacion> findByEstadoActualAndEntidadBeneficiariaIsNotNull(EstadoTrack estado, Pageable pageable);

    // Usado por NecesidadService.eliminar(): el borrado físico de una
    // Necesidad se bloquea si CUALQUIER Donacion -- en cualquier estado,
    // incluidos los terminales (ENTREGADA/ENTREGA_FALLIDA/VENCIDA) -- la
    // tiene como necesidadAsignada. Nunca se desvincula una donación
    // histórica solo para permitir el borrado de la necesidad.
    boolean existsByNecesidadAsignada_Id(Long necesidadId);

    // Usado por GestorDonaciones.eliminarSolicitud(): una SolicitudDonacion
    // solo puede borrarse físicamente si todavía no generó ninguna Donacion.
    boolean existsBySolicitudOrigen_Id(Long solicitudId);
}
