package ar.edu.utn.donatrack.notificaciones.model;

import ar.edu.utn.donatrack.notificaciones.enums.EstadoNotificacion;
import ar.edu.utn.donatrack.notificaciones.enums.MedioComunicacion;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Representa una notificacion a enviar a un destinatario por un medio de
 * comunicacion determinado. En esta entrega no hay persistencia externa: las
 * instancias quedan registradas en memoria para trazabilidad.
 */
public class Notificacion {

    private String id;
    private String mensaje;
    private MedioComunicacion medio;
    private String contacto;
    private EstadoNotificacion estado;
    private String servicioOrigen;
    private String fechaCreacion;

    public Notificacion() {
        this.id = UUID.randomUUID().toString();
        this.estado = EstadoNotificacion.PENDIENTE;
        this.fechaCreacion = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public Notificacion(String mensaje, MedioComunicacion medio, String contacto, String servicioOrigen) {
        this();
        this.mensaje = mensaje;
        this.medio = medio;
        this.contacto = contacto;
        this.servicioOrigen = servicioOrigen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public MedioComunicacion getMedio() {
        return medio;
    }

    public void setMedio(MedioComunicacion medio) {
        this.medio = medio;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public EstadoNotificacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoNotificacion estado) {
        this.estado = estado;
    }

    public String getServicioOrigen() {
        return servicioOrigen;
    }

    public void setServicioOrigen(String servicioOrigen) {
        this.servicioOrigen = servicioOrigen;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public void marcarCompletada() {
        this.estado = EstadoNotificacion.COMPLETADA;
    }
}
