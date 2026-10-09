package ar.edu.utn.frba.dds.incentivos.repository;

import ar.edu.utn.frba.dds.incentivos.misiones.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    List<Categoria> findAllByOrderByOrdenAsc();
}
