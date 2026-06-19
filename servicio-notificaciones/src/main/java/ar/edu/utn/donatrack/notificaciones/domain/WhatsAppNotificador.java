package ar.edu.utn.donatrack.notificaciones.strategy;

import ar.edu.utn.donatrack.notificaciones.model.Notificacion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Implementación de Notificador para el medio WHATSAPP.
 * Corresponde a la clase "WhatsAppNotificador" del diagrama de clases.
 *
 * Se simula la llamada al proveedor externo (por ejemplo, WhatsApp
 * Business API). La integración real queda prevista para una entrega futura.
 */
@Slf4j
@Component("whatsAppNotificador")
public class WhatsAppNotificador implements Notificador {

    @Override
    public void enviarNotificacion(Notificacion notificacion) {
        // integrar con proveedor real de WhatsApp Business API (entrega futura).

}
