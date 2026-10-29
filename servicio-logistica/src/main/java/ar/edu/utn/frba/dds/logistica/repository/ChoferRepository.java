package ar.edu.utn.frba.dds.logistica.repository;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Chofer;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChoferRepository extends MongoRepository<Chofer, Integer> {
    List<Chofer> findByHabilitadoTrueOrderByIdChofer();
}
