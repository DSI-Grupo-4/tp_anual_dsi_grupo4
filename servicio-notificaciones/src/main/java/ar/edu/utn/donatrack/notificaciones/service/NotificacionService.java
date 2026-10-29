package ar.edu.utn.donatrack.notificaciones.service;

import ar.edu.utn.donatrack.notificaciones.dto.NotificacionRequestDTO;
import ar.edu.utn.donatrack.notificaciones.exception.EnvioNotificacionException;
import ar.edu.utn.donatrack.notificaciones.model.Notificacion;
import ar.edu.utn.donatrack.notificaciones.repository.NotificacionRepository;
import ar.edu.utn.donatrack.notificaciones.strategy.Notificador;
import ar.edu.utn.donatrack.notificaciones.strategy.NotificadorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NotificacionService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionService.class);

    private final NotificadorFactory notificadorFactory;
    private final NotificacionRepository repository;

    public NotificacionService(NotificadorFactory notificadorFactory, NotificacionRepository repository) {
        this.notificadorFactory = notificadorFactory;
        this.repository = repository;
    }

    /**
     * Idempotente por servicioOrigen + eventoId: si la cola reentrega un
     * aviso que ya se envió, no se vuelve a mandar. Si el intento anterior
     * había fallado, se reintenta sobre la misma fila.
     */
    public Notificacion enviarNotificacion(NotificacionRequestDTO request) {
        String origen = normalizarOrigen(request.getServicioOrigen());

        Optional<Notificacion> existente =
                repository.findByServicioOrigenAndEventoId(origen, request.getEventoId());

        if (existente.isPresent() && existente.get().estaCompletada()) {
            logger.info("Evento {}:{} ya notificado, se ignora el reintento", origen, request.getEventoId());
            return existente.get();
        }

        Notificacion notificacion = existente.orElseGet(() -> new Notificacion(
                request.getMensaje(),
                request.getMedio(),
                request.getContacto(),
                origen,
                request.getTipoEvento(),
                request.getEventoId()
        ));

        return despachar(notificacion);
    }

    /**
     * Intenta el envío y guarda el resultado, salga bien o mal.
     * Si falla, relanza la excepción para que RabbitMQ reintente
     * (y después de 3 intentos lo mande a la cola de fallidos).
     */
    public Notificacion despachar(Notificacion notificacion) {
        notificacion.registrarIntento();
        try {
            Notificador notificador = notificadorFactory.obtenerNotificador(notificacion.getMedio());
            notificador.enviarNotificacion(notificacion);
            notificacion.marcarCompletada();
            return repository.save(notificacion);
        } catch (IllegalArgumentException ex) {
            notificacion.marcarFallida(ex.getMessage());
            repository.save(notificacion);
            throw ex;
        } catch (Exception ex) {
            notificacion.marcarFallida(ex.getMessage());
            repository.save(notificacion);
            throw new EnvioNotificacionException(
                    "No se pudo enviar la notificacion " + notificacion.getId() + " por el medio " + notificacion.getMedio(),
                    ex
            );
        }
    }

    public Optional<Notificacion> buscarPorId(String id) {
        return repository.findById(id);
    }

    public List<Notificacion> listarTodas() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "fechaCreacion"));
    }

    // Mismo criterio que el constructor de Notificacion, para que la búsqueda encuentre lo que se guardó
    private String normalizarOrigen(String origen) {
        return (origen == null || origen.isBlank()) ? "desconocido" : origen;
    }
}