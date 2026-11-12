package ar.edu.utn.frba.dds.donaciones.repository;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonacionRepository extends JpaRepository<Donacion, Long>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<Donacion> {

    @org.springframework.data.jpa.repository.Query("""
        select new ar.edu.utn.frba.dds.donaciones.dto.DonacionDestacadaDTO(
            i.categoria, i.subcategoria, sum(i.cantidad), i.unidadMedida, e.entidad.razonSocial, c.fechaCambio)
        from Donacion d join d.items i join d.historialEstados c join d.entidadBeneficiaria e
        where d.estadoActual = :estado and c.estadoNuevo = :estado and c.fechaCambio between :desde and :hasta
        group by d.id, i.categoria, i.subcategoria, i.unidadMedida, e.entidad.razonSocial, c.fechaCambio
        order by c.fechaCambio desc, d.id desc
        """)
    List<ar.edu.utn.frba.dds.donaciones.dto.DonacionDestacadaDTO> destacadas(EstadoTrack estado,
            java.time.LocalDateTime desde, java.time.LocalDateTime hasta, Pageable pagina);

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
