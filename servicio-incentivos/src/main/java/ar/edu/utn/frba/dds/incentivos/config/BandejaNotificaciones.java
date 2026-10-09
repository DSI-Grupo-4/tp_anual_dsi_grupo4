package ar.edu.utn.frba.dds.incentivos.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Bandeja durable local: confirma recepción y enrutamiento antes de borrar cada solicitud. */
@Component
public class BandejaNotificaciones {
    private final RabbitTemplate rabbit;
    private final ObjectMapper mapper;
    private final Path directorio;
    public BandejaNotificaciones(RabbitTemplate rabbit, ObjectMapper mapper,
            @Value("${notificaciones.pendientes-dir:.datos/incentivos/notificaciones-pendientes}") String directorio) {
        this.rabbit = rabbit; this.mapper = mapper; this.directorio = Path.of(directorio);
    }
    public synchronized void guardar(Object solicitud) {
        Map<String,Object> datos = mapper.convertValue(solicitud, new TypeReference<Map<String,Object>>() {});
        String id = UUID.nameUUIDFromBytes(datos.get("eventoId").toString().getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
        try {
            Files.createDirectories(directorio);
            Path destino = directorio.resolve(id + ".json");
            if (Files.exists(destino)) return;
            Path temporal = Files.createTempFile(directorio, "pendiente-", ".tmp");
            mapper.writeValue(temporal.toFile(), datos);
            Files.move(temporal, destino, StandardCopyOption.ATOMIC_MOVE);
        } catch (java.io.IOException e) { throw new IllegalStateException("No se pudo guardar la notificación pendiente", e); }
    }
    @Scheduled(fixedDelayString = "${notificaciones.reintento-ms:5000}")
    public void publicarPendientes() {
        if (!Files.exists(directorio)) return;
        try (var archivos = Files.list(directorio)) {
            for (Path archivo : archivos.filter(p -> p.toString().endsWith(".json")).sorted().toList()) {
                try {
                    Map<String,Object> solicitud = mapper.readValue(archivo.toFile(), new TypeReference<Map<String,Object>>() {});
                    CorrelationData correlacion = new CorrelationData(UUID.randomUUID().toString());
                    rabbit.convertAndSend("donatrack.notificaciones", "notificacion.solicitada", solicitud, correlacion);
                    var confirmacion = correlacion.getFuture().get(5, TimeUnit.SECONDS);
                    if (!confirmacion.isAck() || correlacion.getReturned() != null)
                        throw new IllegalStateException("RabbitMQ no confirmó el enrutamiento");
                    Files.delete(archivo);
                } catch (Exception e) {
                    if (e instanceof InterruptedException) Thread.currentThread().interrupt();
                    org.slf4j.LoggerFactory.getLogger(getClass()).warn("Notificación pendiente {}: {}", archivo.getFileName(), e.getMessage());
                    break;
                }
            }
        } catch (java.io.IOException e) { throw new IllegalStateException("No se pudo leer la bandeja de notificaciones", e); }
    }
}
