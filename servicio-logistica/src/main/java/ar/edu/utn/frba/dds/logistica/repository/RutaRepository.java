package ar.edu.utn.frba.dds.logistica.repository;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Camion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Chofer;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoRuta;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface RutaRepository extends JpaRepository<Ruta, Integer> {
    long countByChoferAndEstadoRutaIn(Chofer chofer, Collection<EstadoRuta> estados);

    boolean existsByChofer(Chofer chofer);

    boolean existsByCamionAsociado(Camion camion);
}
