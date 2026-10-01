package ar.edu.utn.donatrack.notificaciones.messaging;

import ar.edu.utn.donatrack.notificaciones.config.RabbitNotificacionesConfig;
import ar.edu.utn.donatrack.notificaciones.dto.NotificacionRequestDTO;
import ar.edu.utn.donatrack.notificaciones.service.NotificacionService;
import jakarta.validation.Valid;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class NotificacionSolicitadaConsumer {

    private final NotificacionService notificacionService;

    public NotificacionSolicitadaConsumer(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @RabbitListener(queues = RabbitNotificacionesConfig.QUEUE)
    public void consumir(@Valid NotificacionRequestDTO solicitud) {
        notificacionService.enviarNotificacion(solicitud);
    }
}
