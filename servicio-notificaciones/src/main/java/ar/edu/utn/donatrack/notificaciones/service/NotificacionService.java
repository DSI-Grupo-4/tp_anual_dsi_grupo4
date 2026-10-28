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
    // Idempotencia -- un reintento de la cola con el mismo eventoId no debe
    // volver a despachar la notificación. Mapa separado (no basta con
    // notificacionesPorId porque el id interno se genera random en cada intento).
    private final Map<String, Notificacion> notificacionesPorEventoId = new ConcurrentHashMap<>();

    private final com.fasterxml.jackson.databind.ObjectMapper mapper;
    private final java.nio.file.Path historial;

    @org.springframework.beans.factory.annotation.Autowired
    public NotificacionService(NotificadorFactory factory, com.fasterxml.jackson.databind.ObjectMapper mapper,
            @org.springframework.beans.factory.annotation.Value("${notificaciones.historial-dir:.datos/notificaciones/historial}") String directorio) {
        this.notificadorFactory = factory; this.mapper = mapper; this.historial = java.nio.file.Path.of(directorio);
        if (java.nio.file.Files.exists(historial)) {
            try (var archivos = java.nio.file.Files.list(historial)) {
                for (var archivo : archivos.filter(p -> p.toString().endsWith(".json")).toList()) {
                    Notificacion n = mapper.readValue(archivo.toFile(), Notificacion.class);
                    notificacionesPorId.put(n.getId(), n);
                    notificacionesPorEventoId.put(clave(n.getServicioOrigen(), n.getEventoId()), n);
                }
            } catch (java.io.IOException e) { throw new IllegalStateException("No se pudo recuperar el historial de notificaciones", e); }
        }
    }
    private String clave(String origen, String eventoId) { return String.valueOf(origen) + ":" + eventoId; }

    public synchronized Notificacion enviarNotificacion(NotificacionRequestDTO request) {
        Notificacion existente = notificacionesPorEventoId.get(clave(request.getServicioOrigen(), request.getEventoId()));
        if (existente != null) {
            return existente;
        }

        Notificacion notificacion = new Notificacion(
                request.getMensaje(),
                request.getMedio(),
                request.getContacto(),
                request.getServicioOrigen(),
                request.getTipoEvento(),
                request.getEventoId()
        );

        Notificacion despachada = despachar(notificacion);
        notificacionesPorEventoId.put(clave(request.getServicioOrigen(), request.getEventoId()), despachada);
        return despachada;
    }

    public Notificacion despachar(Notificacion notificacion) {
        try {
            Notificador notificador = notificadorFactory.obtenerNotificador(notificacion.getMedio());
            notificador.enviarNotificacion(notificacion);
            java.nio.file.Files.createDirectories(historial);
            var temporal = java.nio.file.Files.createTempFile(historial, "notificacion-", ".tmp");
            mapper.writeValue(temporal.toFile(), notificacion);
            java.nio.file.Files.move(temporal, historial.resolve(notificacion.getId() + ".json"), java.nio.file.StandardCopyOption.ATOMIC_MOVE);
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
