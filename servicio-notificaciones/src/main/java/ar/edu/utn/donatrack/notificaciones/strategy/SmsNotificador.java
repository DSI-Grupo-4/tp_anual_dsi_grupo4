package ar.edu.utn.donatrack.notificaciones.strategy;

import ar.edu.utn.donatrack.notificaciones.model.Notificacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("smsNotificador")
public class SmsNotificador implements Notificador {

    private static final Logger logger = LoggerFactory.getLogger(SmsNotificador.class);

    @Override
    public void enviarNotificacion(Notificacion notificacion) {
        logger.info("Simulando envio de notificacion por SMS a {}: {}", notificacion.getContacto(), notificacion.getMensaje());
        notificacion.marcarCompletada();
    }
}
