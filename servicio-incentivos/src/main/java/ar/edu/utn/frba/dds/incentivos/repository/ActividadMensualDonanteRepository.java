package ar.edu.utn.frba.dds.incentivos.repository;

import ar.edu.utn.frba.dds.incentivos.ranking.ActividadMensualDonante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActividadMensualDonanteRepository extends JpaRepository<ActividadMensualDonante, Integer> {
    // ranking null = contador del mes en curso, todavía no cerrado.
    List<ActividadMensualDonante> findByRankingIsNull();
}
