package ar.edu.utn.frba.dds.donaciones.repository;

import ar.edu.utn.frba.dds.donaciones.domain.personas.Donante;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonanteRepository extends JpaRepository<Donante, Long> {
}
