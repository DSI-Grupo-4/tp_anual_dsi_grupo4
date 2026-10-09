package ar.edu.utn.frba.dds.logistica.repository;

import ar.edu.utn.frba.dds.logistica.domain.eventos.EventoLogistico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventoLogisticoRepository extends JpaRepository<EventoLogistico, UUID> {
    List<EventoLogistico> findByPublicadoFalseOrderByFechaGeneracionAsc();
}
