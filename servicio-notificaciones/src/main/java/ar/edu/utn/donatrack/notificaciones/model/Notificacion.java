package ar.edu.utn.donatrack.notificaciones.model;

import ar.edu.utn.donatrack.notificaciones.enums.EstadoNotificacion;
import ar.edu.utn.donatrack.notificaciones.enums.MedioComunicacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Notificación a enviar a un destinatario por un medio de comunicación.
 * Se persiste en la tabla "notificacion" del esquema "notificaciones".
 * La combinación servicioOrigen + eventoId es única: garantiza que un
 * reintento de la cola no genere un segundo envío.
 */
@Entity
@Table(name = "notificacion",
        uniqueConstraints = @UniqueConstraint(name = "uk_notificacion_origen_evento",
                columnNames = {"servicio_origen", "evento_id"}))
public class Notificacion {

    private static final int MAX_DETALLE_ERROR = 500;

    // CHAR(36) en MySQL: sin @JdbcTypeCode, Hibernate espera VARCHAR y la validación falla
    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "mensaje", nullable = false, length = 2000)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(name = "medio", nullable = false)
    private MedioComunicacion medio;

    @Column(name = "contacto", nullable = false, length = 150)
    private String contacto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoNotificacion estado;

    @Column(name = "servicio_origen", nullable = false, length = 50)
    private String servicioOrigen;

    @Column(name = "tipo_evento", nullable = false, length = 50)
    private String tipoEvento;

    @Column(name = "evento_id", nullable = false, length = 200)
    private String eventoId;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "intentos", nullable = false)
    private int intentos;

    @Column(name = "detalle_error", length = MAX_DETALLE_ERROR)
    private String detalleError;

    // JPA necesita un constructor sin argumentos para reconstruir el objeto al leerlo de la base
    public Notificacion() {
        this.id = UUID.randomUUID().toString();
        this.estado = EstadoNotificacion.PENDIENTE;
        this.fechaCreacion = LocalDateTime.now();
    }

    public Notificacion(String mensaje, MedioComunicacion medio, String contacto, String servicioOrigen,
                        String tipoEvento, String eventoId) {
        this();
        this.mensaje = mensaje;
        this.medio = medio;
        this.contacto = contacto;
        this.servicioOrigen = (servicioOrigen == null || servicioOrigen.isBlank()) ? "desconocido" : servicioOrigen;
        this.tipoEvento = tipoEvento;
        this.eventoId = eventoId;
    }

    // --- Comportamiento ---

    public void registrarIntento() {
        this.intentos++;
    }

    public void marcarCompletada() {
        this.estado = EstadoNotificacion.COMPLETADA;
        this.fechaEnvio = LocalDateTime.now();
        this.detalleError = null;
    }

    public void marcarFallida(String error) {
        this.estado = EstadoNotificacion.FALLIDA;
        if (error != null && error.length() > MAX_DETALLE_ERROR) {
            error = error.substring(0, MAX_DETALLE_ERROR);
        }
        this.detalleError = error;
    }

    public boolean estaCompletada() {
        return this.estado == EstadoNotificacion.COMPLETADA;
    }

    // --- Getters y setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public MedioComunicacion getMedio() { return medio; }
    public void setMedio(MedioComunicacion medio) { this.medio = medio; }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }

    public EstadoNotificacion getEstado() { return estado; }
    public void setEstado(EstadoNotificacion estado) { this.estado = estado; }

    public String getServicioOrigen() { return servicioOrigen; }
    public void setServicioOrigen(String servicioOrigen) { this.servicioOrigen = servicioOrigen; }

    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }

    public String getEventoId() { return eventoId; }
    public void setEventoId(String eventoId) { this.eventoId = eventoId; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaEnvio() { return fechaEnvio; }

    public int getIntentos() { return intentos; }

    public String getDetalleError() { return detalleError; }
}