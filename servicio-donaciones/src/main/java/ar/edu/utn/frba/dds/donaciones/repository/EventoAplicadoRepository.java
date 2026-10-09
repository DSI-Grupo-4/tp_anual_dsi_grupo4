package ar.edu.utn.frba.dds.donaciones.repository;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EventoAplicado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoAplicadoRepository extends JpaRepository<EventoAplicado, String> {
}
