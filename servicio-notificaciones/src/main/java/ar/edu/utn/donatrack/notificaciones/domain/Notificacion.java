package ar.edu.utn.donatrack.notificaciones.model;

import ar.edu.utn.donatrack.notificaciones.enums.EstadoNotificacion;
import ar.edu.utn.donatrack.notificaciones.enums.MedioComunicacion;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa una notificación a enviar a un destinatario (persona donante
 * o entidad beneficiaria) por un medio de comunicación determinado.
 *
 * Corresponde a la clase "Notificacion" del diagrama de clases.
 *
 * En esta entrega no hay persistencia (no se usa JPA): las instancias
 * viven en memoria mientras se procesa el envío.
 */
@Getter
@Setter
public class Notificacion {

    private String id;
    private String mensaje;
    private MedioComunicacion medio;
    private String contacto;
    private EstadoNotificacion estado;

    /**
     * Servicio de origen que generó el evento (Donaciones, Incentivos, etc.).
     * No está en el diagrama original, pero es útil para trazabilidad y logs;
     * se completa a partir del campo "origen" del request.
     */
    private String servicioOrigen;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEnvio;

    public Notificacion() {
        this.id = UUID.randomUUID().toString();
        this.estado = EstadoNotificacion.PENDIENTE;
        this.fechaCreacion = LocalDateTime.now();
    }

    public Notificacion(String mensaje, MedioComunicacion medio, String contacto, String servicioOrigen) {
        this();
        this.mensaje = mensaje;
        this.medio = medio;
        this.contacto = contacto;
        this.servicioOrigen = servicioOrigen;
    }

    public void marcarComoCompletada() {
        this.estado = EstadoNotificacion.COMPLETADA;
        this.fechaEnvio = LocalDateTime.now();
    }
}
