package ar.edu.utn.frba.dds.donaciones.repository;

import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NecesidadRepository extends JpaRepository<Necesidad, Long> {
    List<Necesidad> findByEntidadBeneficiaria_Id(Long entidadBeneficiariaId);
}
