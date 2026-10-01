package ar.edu.utn.frba.dds.donaciones.client;

import ar.edu.utn.frba.dds.donaciones.config.RabbitNotificacionesConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificacionesClient {

    private final RabbitTemplate rabbitTemplate;

    public NotificacionesClient(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void enviar(String mensaje, String medio, String contacto) {
        NotificacionRequest payload = new NotificacionRequest(mensaje, medio, contacto, "donaciones");
        rabbitTemplate.convertAndSend(
                RabbitNotificacionesConfig.EXCHANGE,
                RabbitNotificacionesConfig.ROUTING_KEY,
                payload);
    }

    private record NotificacionRequest(String mensaje, String medio, String contacto, String servicioOrigen) {
    }
}
