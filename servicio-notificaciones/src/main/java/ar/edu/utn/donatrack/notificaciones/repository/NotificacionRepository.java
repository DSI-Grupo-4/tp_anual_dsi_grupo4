package ar.edu.utn.donatrack.notificaciones.repository;

import ar.edu.utn.donatrack.notificaciones.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificacionRepository extends JpaRepository<Notificacion, String> {

    // Spring arma la consulta solo, a partir del nombre del método:
    // SELECT * FROM notificacion WHERE servicio_origen = ? AND evento_id = ?
    Optional<Notificacion> findByServicioOrigenAndEventoId(String servicioOrigen, String eventoId);
}