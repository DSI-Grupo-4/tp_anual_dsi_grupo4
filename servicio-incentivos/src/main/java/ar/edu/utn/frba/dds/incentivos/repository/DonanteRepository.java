package ar.edu.utn.frba.dds.incentivos.repository;

import ar.edu.utn.frba.dds.incentivos.donante.Donante;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonanteRepository extends JpaRepository<Donante, Long> {
}
