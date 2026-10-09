package ar.edu.utn.frba.dds.donaciones.repository;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.ItemDonado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemDonadoRepository extends JpaRepository<ItemDonado, Long> {
}
