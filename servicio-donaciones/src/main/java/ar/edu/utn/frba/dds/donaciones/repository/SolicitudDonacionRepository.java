package ar.edu.utn.frba.dds.donaciones.repository;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.SolicitudDonacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitudDonacionRepository extends JpaRepository<SolicitudDonacion, Long> {
}
