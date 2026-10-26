package ar.edu.utn.frba.dds.logistica.repository;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Camion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoCamion;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CamionRepository extends MongoRepository<Camion, Integer> {
    List<Camion> findByEstadoCamionOrderByIdCamion(EstadoCamion estadoCamion);
}
