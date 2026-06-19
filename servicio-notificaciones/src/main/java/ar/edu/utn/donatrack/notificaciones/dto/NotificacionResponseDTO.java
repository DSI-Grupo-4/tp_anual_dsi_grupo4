package ar.edu.utn.donatrack.notificaciones.dto;

import ar.edu.utn.donatrack.notificaciones.enums.EstadoNotificacion;
import ar.edu.utn.donatrack.notificaciones.enums.MedioComunicacion;
import ar.edu.utn.donatrack.notificaciones.model.Notificacion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Representa la respuesta que el Servicio de Notificaciones devuelve
 * al servicio que solicitó el envío, indicando cómo quedó la notificación
 * procesada (id generado, estado final, fechas, etc.).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionResponseDTO {

    private String id;
    private String mensaje;
    private MedioComunicacion medio;
    private String contacto;
    private EstadoNotificacion estado;
    private String servicioOrigen;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEnvio;

    public static NotificacionResponseDTO desde(Notificacion notificacion) {
        return new NotificacionResponseDTO(
                notificacion.getId(),
                notificacion.getMensaje(),
                notificacion.getMedio(),
                notificacion.getContacto(),
                notificacion.getEstado(),
                notificacion.getServicioOrigen(),
                notificacion.getFechaCreacion(),
                notificacion.getFechaEnvio()
        );
    }
}
