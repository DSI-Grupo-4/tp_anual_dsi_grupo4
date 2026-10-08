package ar.edu.utn.frba.dds.incentivos.client;

import ar.edu.utn.frba.dds.incentivos.config.RabbitNotificacionesConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Publica hacia la cola de Notificaciones vía RabbitMQ. Reemplaza al
 * RestTemplate síncrono que tenía Consultor, para que la integración con
 * Notificaciones sea asíncrona.
 *
 * Consultor es un singleton manual (no un bean de Spring), así que esta
 * clase se le inyecta al boot vía NotificacionesClienteConfigurer
 * (ApplicationRunner), mismo patrón que ya usan WebhookN8nConfigurer y
 * NotificacionesConfigurer para las URLs.
 */
@Component
public class NotificacionesClient {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionesClient.class);

    private final RabbitTemplate rabbitTemplate;

    public NotificacionesClient(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * @param medio    EMAIL | SMS | WHATSAPP, tal como lo espera
     *                 servicio-notificaciones. Si es null (el Donante no
     *                 tiene contacto configurado todavía), no se publica
     *                 nada -- no se inventa un contacto.
     */
    public void enviar(String tipoEvento, String mensaje, String medio, String contacto) {
        if (medio == null || contacto == null || contacto.isBlank()) {
            logger.warn("No se pudo notificar el evento '{}': el donante no tiene medio/contacto configurado.",
                    tipoEvento);
            return;
        }

        NotificacionRequest request = new NotificacionRequest(
                mensaje, medio, contacto, "incentivos", tipoEvento, UUID.randomUUID().toString());

        rabbitTemplate.convertAndSend(
                RabbitNotificacionesConfig.EXCHANGE,
                RabbitNotificacionesConfig.ROUTING_KEY,
                request);
    }

    private record NotificacionRequest(String mensaje, String medio, String contacto, String servicioOrigen,
                                        String tipoEvento, String eventoId) {
    }
}
