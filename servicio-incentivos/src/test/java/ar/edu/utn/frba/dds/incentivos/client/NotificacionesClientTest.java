package ar.edu.utn.frba.dds.incentivos.client;

import ar.edu.utn.frba.dds.incentivos.config.RabbitNotificacionesConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificacionesClientTest {

    @Test
    void publicaLaMisionCompletadaConLosDatosDelDestinatario() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        NotificacionesClient client = new NotificacionesClient(rabbitTemplate);
        ArgumentCaptor<Object> mensaje = ArgumentCaptor.forClass(Object.class);

        client.notificarMisionCompletada(
                15, "Primera donacion", "WHATSAPP", "+5491112345678");

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitNotificacionesConfig.EXCHANGE),
                eq(RabbitNotificacionesConfig.ROUTING_KEY),
                mensaje.capture());

        JsonNode json = new ObjectMapper().valueToTree(mensaje.getValue());
        assertEquals("Completaste la mision: Primera donacion.", json.get("mensaje").asText());
        assertEquals("WHATSAPP", json.get("medio").asText());
        assertEquals("+5491112345678", json.get("contacto").asText());
        assertEquals("incentivos", json.get("servicioOrigen").asText());
    }
}
