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
    private String tipoEvento;
    private String eventoId;

    public NotificacionResponseDTO() {
    }

    public NotificacionResponseDTO(String id, String mensaje, MedioComunicacion medio, String contacto,
                                   EstadoNotificacion estado, String servicioOrigen, String fechaCreacion,
                                   String tipoEvento, String eventoId) {
        this.id = id;
        this.mensaje = mensaje;
        this.medio = medio;
        this.contacto = contacto;
        this.estado = estado;
        this.servicioOrigen = servicioOrigen;
        this.fechaCreacion = fechaCreacion;
        this.tipoEvento = tipoEvento;
        this.eventoId = eventoId;
    }

    public static NotificacionResponseDTO desde(Notificacion notificacion) {
        return new NotificacionResponseDTO(
                notificacion.getId(),
                notificacion.getMensaje(),
                notificacion.getMedio(),
                notificacion.getContacto(),
                notificacion.getEstado(),
                notificacion.getServicioOrigen(),
                notificacion.getFechaCreacion(),
                notificacion.getTipoEvento(),
                notificacion.getEventoId()
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

    public String getTipoEvento() {
        return tipoEvento;
    }

    public String getEventoId() {
        return eventoId;
    }
}
