package ar.edu.utn.frba.dds.incentivos.scheduler;

import ar.edu.utn.frba.dds.incentivos.client.NotificacionesClient;
import ar.edu.utn.frba.dds.incentivos.consultor.Consultor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// D-009/D-010: Consultor es un singleton manual, no un bean de Spring --
// este ApplicationRunner le inyecta al boot el NotificacionesClient real
// (RabbitMQ), reemplazando la URL que usaba el RestTemplate síncrono viejo.
@Component
public class NotificacionesConfigurer implements ApplicationRunner {

    private final NotificacionesClient notificacionesClient;

    public NotificacionesConfigurer(NotificacionesClient notificacionesClient) {
        this.notificacionesClient = notificacionesClient;
    }

    @Override
    public void run(ApplicationArguments args) {
        Consultor.getInstance().configurarNotificacionesClient(notificacionesClient);
    }
}
