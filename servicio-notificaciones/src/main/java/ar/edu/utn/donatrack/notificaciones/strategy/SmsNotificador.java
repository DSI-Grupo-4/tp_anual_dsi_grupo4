package ar.edu.utn.donatrack.notificaciones.strategy;

import ar.edu.utn.donatrack.notificaciones.model.Notificacion;

import org.springframework.stereotype.Component;


@Component("smsNotificador")
public class SmsNotificador implements Notificador {

    @Override
    public void enviarNotificacion(Notificacion notificacion) {
        //  integrar con proveedor real de envío de SMS (entrega futura).
    }
}
