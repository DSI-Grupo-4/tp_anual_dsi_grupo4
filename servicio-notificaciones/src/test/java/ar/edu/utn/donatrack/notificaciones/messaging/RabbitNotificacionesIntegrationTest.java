package ar.edu.utn.donatrack.notificaciones.messaging;

import ar.edu.utn.donatrack.notificaciones.config.RabbitNotificacionesConfig;
import ar.edu.utn.donatrack.notificaciones.dto.NotificacionRequestDTO;
import ar.edu.utn.donatrack.notificaciones.enums.MedioComunicacion;
import ar.edu.utn.donatrack.notificaciones.service.NotificacionService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class RabbitNotificacionesIntegrationTest {

    @Container
    static final RabbitMQContainer RABBITMQ =
            new RabbitMQContainer("rabbitmq:4-management");

    @DynamicPropertySource
    static void rabbitProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", RABBITMQ::getHost);
        registry.add("spring.rabbitmq.port", RABBITMQ::getAmqpPort);
        registry.add("spring.rabbitmq.username", RABBITMQ::getAdminUsername);
        registry.add("spring.rabbitmq.password", RABBITMQ::getAdminPassword);
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private NotificacionService notificacionService;

    @Test
    void publicaEnRabbitYElConsumerRecibeLaSolicitud() {
        NotificacionRequestDTO solicitud = new NotificacionRequestDTO(
                "Donacion asignada",
                MedioComunicacion.EMAIL,
                "donante@correo.com",
                "donaciones",
                "DONACION_ASIGNADA",
                "evt-integration-1");

        rabbitTemplate.convertAndSend(
                RabbitNotificacionesConfig.EXCHANGE,
                RabbitNotificacionesConfig.ROUTING_KEY,
                solicitud);

        verify(notificacionService, timeout(10_000)).enviarNotificacion(argThat(recibida ->
                "Donacion asignada".equals(recibida.getMensaje())
                        && recibida.getMedio() == MedioComunicacion.EMAIL
                        && "donante@correo.com".equals(recibida.getContacto())
                        && "donaciones".equals(recibida.getServicioOrigen())));
    }
}
