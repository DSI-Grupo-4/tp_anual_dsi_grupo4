package ar.edu.utn.frba.dds.incentivos.repository;

import ar.edu.utn.frba.dds.incentivos.consultor.Beneficiario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeneficiarioRepository extends JpaRepository<Beneficiario, Long> {
}
