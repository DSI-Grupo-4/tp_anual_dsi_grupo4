package ar.edu.utn.frba.dds.donaciones.repository;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonacionRepository extends JpaRepository<Donacion, Long> {
}
