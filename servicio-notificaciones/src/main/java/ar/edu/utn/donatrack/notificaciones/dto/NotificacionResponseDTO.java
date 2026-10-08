package ar.edu.utn.donatrack.notificaciones.dto;

import ar.edu.utn.donatrack.notificaciones.enums.EstadoNotificacion;
import ar.edu.utn.donatrack.notificaciones.enums.MedioComunicacion;
import ar.edu.utn.donatrack.notificaciones.model.Notificacion;

/**
 * Representa la respuesta que el Servicio de Notificaciones devuelve al
 * servicio que solicito el envio.
 */
public class NotificacionResponseDTO {

    private String id;
    private String mensaje;
    private MedioComunicacion medio;
    private String contacto;
    private EstadoNotificacion estado;
    private String servicioOrigen;
    private String fechaCreacion;

    public NotificacionResponseDTO() {
    }

    public NotificacionResponseDTO(String id, String mensaje, MedioComunicacion medio, String contacto,
                                   EstadoNotificacion estado, String servicioOrigen, String fechaCreacion) {
        this.id = id;
        this.mensaje = mensaje;
        this.medio = medio;
        this.contacto = contacto;
        this.estado = estado;
        this.servicioOrigen = servicioOrigen;
        this.fechaCreacion = fechaCreacion;
    }

    public static NotificacionResponseDTO desde(Notificacion notificacion) {
        return new NotificacionResponseDTO(
                notificacion.getId(),
                notificacion.getMensaje(),
                notificacion.getMedio(),
                notificacion.getContacto(),
                notificacion.getEstado(),
                notificacion.getServicioOrigen(),
                notificacion.getFechaCreacion()
        );
    }

    public String getId() {
        return id;
    }

    public String getMensaje() {
        return mensaje;
    }

    public MedioComunicacion getMedio() {
        return medio;
    }

    public String getContacto() {
        return contacto;
    }

    public EstadoNotificacion getEstado() {
        return estado;
    }

    public String getServicioOrigen() {
        return servicioOrigen;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }
}
