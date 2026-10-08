package ar.edu.utn.donatrack.notificaciones.service;

import ar.edu.utn.donatrack.notificaciones.dto.NotificacionRequestDTO;
import ar.edu.utn.donatrack.notificaciones.exception.EnvioNotificacionException;
import ar.edu.utn.donatrack.notificaciones.model.Notificacion;
import ar.edu.utn.donatrack.notificaciones.strategy.Notificador;
import ar.edu.utn.donatrack.notificaciones.strategy.NotificadorFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificacionService {

    private final NotificadorFactory notificadorFactory;
    private final Map<String, Notificacion> notificacionesPorId = new ConcurrentHashMap<>();

    public NotificacionService(NotificadorFactory notificadorFactory) {
        this.notificadorFactory = notificadorFactory;
    }

    public Notificacion enviarNotificacion(NotificacionRequestDTO request) {
        Notificacion notificacion = new Notificacion(
                request.getMensaje(),
                request.getMedio(),
                request.getContacto(),
                request.getServicioOrigen()
        );

        return despachar(notificacion);
    }

    public Notificacion despachar(Notificacion notificacion) {
        try {
            Notificador notificador = notificadorFactory.obtenerNotificador(notificacion.getMedio());
            notificador.enviarNotificacion(notificacion);
            notificacionesPorId.put(notificacion.getId(), notificacion);
            return notificacion;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new EnvioNotificacionException(
                    "No se pudo enviar la notificacion " + notificacion.getId() + " por el medio " + notificacion.getMedio(),
                    ex
            );
        }
    }

    public Optional<Notificacion> buscarPorId(String id) {
        return Optional.ofNullable(notificacionesPorId.get(id));
    }

    public List<Notificacion> listarTodas() {
        return new ArrayList<>(notificacionesPorId.values());
    }
}
