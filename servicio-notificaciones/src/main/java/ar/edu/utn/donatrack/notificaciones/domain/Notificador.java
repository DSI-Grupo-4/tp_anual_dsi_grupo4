package ar.edu.utn.donatrack.notificaciones.strategy;

import ar.edu.utn.donatrack.notificaciones.model.Notificacion;


public interface Notificador {

    /**
     * Envía (o simula el envío de) la notificación recibida.
     *
     * @param notificacion notificación a enviar, con mensaje y contacto ya completos.
     */
    void enviarNotificacion(Notificacion notificacion);
}
