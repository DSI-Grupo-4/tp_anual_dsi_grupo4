package ar.edu.utn.frba.dds.incentivos.scheduler;

import ar.edu.utn.frba.dds.incentivos.consultor.Consultor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class NotificacionesConfigurer implements ApplicationRunner {

    private final String notificacionesBaseUrl;

    public NotificacionesConfigurer(@Value("${incentivos.notificaciones.base-url:}") String notificacionesBaseUrl) {
        this.notificacionesBaseUrl = notificacionesBaseUrl;
    }

    @Override
    public void run(ApplicationArguments args) {
        Consultor.getInstance().configurarNotificaciones(notificacionesBaseUrl);
    }
}
