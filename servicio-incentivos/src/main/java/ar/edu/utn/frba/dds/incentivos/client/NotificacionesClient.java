package ar.edu.utn.frba.dds.incentivos.client;

import ar.edu.utn.frba.dds.incentivos.config.RabbitNotificacionesConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificacionesClient {

    private final RabbitTemplate rabbitTemplate;

    public NotificacionesClient(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void notificarMisionCompletada(int donanteId, String misionNombre) {
        notificarMisionCompletada(donanteId, misionNombre, null, null);
    }

    public void notificarMisionCompletada(int donanteId, String misionNombre, String medio, String contacto) {
        enviar(
                "Completaste la mision: " + misionNombre + ".",
                medioPreferidoODefault(medio),
                contactoPreferidoODefault(donanteId, contacto)
        );
    }

    public void notificarSubidaCategoria(int donanteId, String nuevaCategoria) {
        notificarSubidaCategoria(donanteId, nuevaCategoria, null, null);
    }

    public void notificarSubidaCategoria(int donanteId, String nuevaCategoria, String medio, String contacto) {
        enviar(
                "Subiste de categoria a: " + nuevaCategoria + ".",
                medioPreferidoODefault(medio),
                contactoPreferidoODefault(donanteId, contacto)
        );
    }

    private void enviar(String mensaje, String medio, String contacto) {
        NotificacionRequest payload = new NotificacionRequest(mensaje, medio, contacto, "incentivos");
        rabbitTemplate.convertAndSend(
                RabbitNotificacionesConfig.EXCHANGE,
                RabbitNotificacionesConfig.ROUTING_KEY,
                payload);
    }

    private String medioPreferidoODefault(String medio) {
        return medio == null || medio.isBlank() ? "EMAIL" : medio;
    }

    private String contactoPreferidoODefault(int donanteId, String contacto) {
        return contacto == null || contacto.isBlank() ? "donante" + donanteId + "@donatrack.local" : contacto;
    }

    private record NotificacionRequest(String mensaje, String medio, String contacto, String servicioOrigen) {
    }
}
