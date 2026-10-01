package ar.edu.utn.frba.dds.donaciones.client;

import ar.edu.utn.frba.dds.donaciones.config.RabbitNotificacionesConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificacionesClientTest {

    @Test
    void publicaLaSolicitudEnElExchangeDeNotificaciones() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        NotificacionesClient client = new NotificacionesClient(rabbitTemplate);
        ArgumentCaptor<Object> mensaje = ArgumentCaptor.forClass(Object.class);

        client.enviar("Donacion asignada", "EMAIL", "donante@correo.com");

        verify(rabbitTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.eq(RabbitNotificacionesConfig.EXCHANGE),
                org.mockito.ArgumentMatchers.eq(RabbitNotificacionesConfig.ROUTING_KEY),
                mensaje.capture());

        JsonNode json = new ObjectMapper().valueToTree(mensaje.getValue());
        assertEquals("Donacion asignada", json.get("mensaje").asText());
        assertEquals("EMAIL", json.get("medio").asText());
        assertEquals("donante@correo.com", json.get("contacto").asText());
        assertEquals("donaciones", json.get("servicioOrigen").asText());
    }
}
