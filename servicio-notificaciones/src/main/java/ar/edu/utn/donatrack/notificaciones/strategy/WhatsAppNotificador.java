package ar.edu.utn.donatrack.notificaciones.strategy;

import ar.edu.utn.donatrack.notificaciones.model.Notificacion;
import ar.edu.utn.donatrack.notificaciones.proveedores.TwilioCliente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("whatsAppNotificador")
public class WhatsAppNotificador implements Notificador {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppNotificador.class);

    private final TwilioCliente twilio;

    public WhatsAppNotificador(TwilioCliente twilio) {
        this.twilio = twilio;
    }

    @Override
    public void enviarNotificacion(Notificacion notificacion) {
        if (!twilio.puedeEnviarWhatsApp()) {
            logger.info("Simulando WhatsApp a {}: {}", notificacion.getContacto(), notificacion.getMensaje());
            return;
        }
        String sid = twilio.enviarWhatsApp(notificacion.getContacto(), notificacion.getMensaje());
        logger.info("WhatsApp enviado a {} (Twilio SID {})", notificacion.getContacto(), sid);
    }
}