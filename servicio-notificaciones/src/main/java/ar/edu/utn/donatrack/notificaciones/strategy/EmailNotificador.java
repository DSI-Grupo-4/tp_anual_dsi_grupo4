package ar.edu.utn.donatrack.notificaciones.strategy;

import ar.edu.utn.donatrack.notificaciones.model.Notificacion;

import org.springframework.stereotype.Component;




@Component("emailNotificador")
public class EmailNotificador implements Notificador {

    @Override
    public void enviarNotificacion(Notificacion notificacion) {
        // integrar con proveedor real de envío de emails (entrega futura).

    }


}
