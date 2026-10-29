package ar.edu.utn.donatrack.notificaciones.strategy;

import ar.edu.utn.donatrack.notificaciones.model.Notificacion;
import ar.edu.utn.donatrack.notificaciones.proveedores.TwilioCliente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("smsNotificador")
public class SmsNotificador implements Notificador {

    private static final Logger logger = LoggerFactory.getLogger(SmsNotificador.class);

    private final TwilioCliente twilio;

    public SmsNotificador(TwilioCliente twilio) {
        this.twilio = twilio;
    }

    @Override
    public void enviarNotificacion(Notificacion notificacion) {
        if (!twilio.puedeEnviarSms()) {
            logger.info("Simulando SMS a {} (sin número de Twilio para SMS): {}",
                    notificacion.getContacto(), notificacion.getMensaje());
            return;
        }
        String sid = twilio.enviarSms(notificacion.getContacto(), notificacion.getMensaje());
        logger.info("SMS enviado a {} (Twilio SID {})", notificacion.getContacto(), sid);
    }
}