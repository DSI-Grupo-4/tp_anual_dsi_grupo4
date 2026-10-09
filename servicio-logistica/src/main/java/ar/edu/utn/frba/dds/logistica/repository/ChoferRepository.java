package ar.edu.utn.frba.dds.logistica.repository;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Chofer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChoferRepository extends JpaRepository<Chofer, Integer> {
    List<Chofer> findByHabilitadoTrueOrderByIdChofer();
}
