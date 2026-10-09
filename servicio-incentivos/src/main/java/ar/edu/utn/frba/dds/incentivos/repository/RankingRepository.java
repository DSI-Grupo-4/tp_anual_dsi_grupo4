package ar.edu.utn.frba.dds.incentivos.repository;

import ar.edu.utn.frba.dds.incentivos.ranking.Ranking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RankingRepository extends JpaRepository<Ranking, Integer> {
    List<Ranking> findAllByOrderByFechaEmisionAsc();
}
