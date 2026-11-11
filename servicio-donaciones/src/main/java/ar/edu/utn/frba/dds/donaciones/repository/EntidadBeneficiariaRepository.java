package ar.edu.utn.frba.dds.donaciones.repository;

import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntidadBeneficiariaRepository extends JpaRepository<EntidadBeneficiaria, Long>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<EntidadBeneficiaria> {
}
