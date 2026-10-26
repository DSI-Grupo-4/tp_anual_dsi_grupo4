package ar.edu.utn.frba.dds.logistica.repository;

import ar.edu.utn.frba.dds.logistica.domain.eventos.EventoLogistico;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface EventoLogisticoRepository extends MongoRepository<EventoLogistico, UUID> {
    List<EventoLogistico> findByPublicadoFalseOrderByFechaGeneracionAsc();
}
