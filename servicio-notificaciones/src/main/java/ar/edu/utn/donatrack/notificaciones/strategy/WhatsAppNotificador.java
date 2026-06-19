package ar.edu.utn.donatrack.notificaciones.strategy;

import ar.edu.utn.donatrack.notificaciones.model.Notificacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Simula la llamada al proveedor externo de WhatsApp. La integracion real
 * queda prevista para una entrega futura.
 */
@Component("whatsAppNotificador")
public class WhatsAppNotificador implements Notificador {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppNotificador.class);

    @Override
    public void enviarNotificacion(Notificacion notificacion) {
        logger.info("Simulando envio de notificacion por WHATSAPP a {}: {}", notificacion.getContacto(), notificacion.getMensaje());
        notificacion.marcarCompletada();
    }
}
