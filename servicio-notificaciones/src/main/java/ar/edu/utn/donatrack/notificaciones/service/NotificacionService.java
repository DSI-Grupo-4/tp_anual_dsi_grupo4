package ar.edu.utn.donatrack.notificaciones.service;

import ar.edu.utn.donatrack.notificaciones.dto.NotificacionRequestDTO;
import ar.edu.utn.donatrack.notificaciones.exception.EnvioNotificacionException;
import ar.edu.utn.donatrack.notificaciones.model.Notificacion;
import ar.edu.utn.donatrack.notificaciones.strategy.Notificador;
import ar.edu.utn.donatrack.notificaciones.strategy.NotificadorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
public class NotificacionService {

    private final NotificadorFactory notificadorFactory;

    public NotificacionService(NotificadorFactory notificadorFactory) {
        this.notificadorFactory = notificadorFactory;
    }

    /**
     * Procesa una solicitud de envío de notificación de punta a punta:
     * crea la notificación en estado PENDIENTE, ejecuta el envío a través
     * del Notificador correspondiente al medio indicado, y la marca como
     * COMPLETADA al finalizar.
     * @return la notificación procesada, con su estado final.
     */
    public Notificacion enviarNotificacion(NotificacionRequestDTO request) {
        Notificacion notificacion = new Notificacion(
                request.getMensaje(),
                request.getMedio(),
                request.getContacto(),
                request.getServicioOrigen()
        );


        try {
            Notificador notificador = notificadorFactory.obtenerNotificador(notificacion.getMedio());
            notificador.enviarNotificacion(notificacion);
            notificacion.marcarComoCompletada();
        } catch (IllegalArgumentException ex) {
            // Medio no soportado: se re-lanza para que el controller devuelva 400.
            throw ex;
        } catch (Exception ex) {
            throw new EnvioNotificacionException(
                    "No se pudo enviar la notificación " + notificacion.getId() + " por el medio " + notificacion.getMedio(),
                    ex
            );
        }


        return notificacion;
    }
}
