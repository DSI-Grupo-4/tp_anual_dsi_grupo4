package ar.edu.utn.donatrack.notificaciones.strategy;

import ar.edu.utn.donatrack.notificaciones.model.Notificacion;

/**
 * No se integra todavía con proveedores externos (SendGrid, Twilio, API de
 * WhatsApp Business, etc.), pero la interfaz ya queda lista para que esa
 * integración se incorpore en una entrega futura sin modificar al resto
 * del servicio (principio Open/Closed).
 */
public interface Notificador {

    /**
     * Envía (o simula el envío de) la notificación recibida.
     *
     * @param notificacion notificación a enviar, con mensaje y contacto ya completos.
     */
    void enviarNotificacion(Notificacion notificacion);
}
