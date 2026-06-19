package ar.edu.utn.donatrack.notificaciones.strategy;

import ar.edu.utn.donatrack.notificaciones.model.Notificacion;

import org.springframework.stereotype.Component;

/**
 * Se simula la llamada al proveedor externo (por ejemplo, WhatsApp
 * Business API). La integración real queda prevista para una entrega futura.
 */

@Component("whatsAppNotificador")
public class WhatsAppNotificador implements Notificador {

    @Override
    public void enviarNotificacion(Notificacion notificacion) {
        // integrar con proveedor real de WhatsApp Business API (entrega futura).
    }
}
