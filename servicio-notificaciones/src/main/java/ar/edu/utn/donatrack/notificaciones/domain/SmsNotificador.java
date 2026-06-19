package ar.edu.utn.donatrack.notificaciones.strategy;

import ar.edu.utn.donatrack.notificaciones.model.Notificacion;

import org.springframework.stereotype.Component;

/**
 * Implementación de Notificador para el medio SMS.
 *
 * Nota de diseño: en el diagrama de clases original esta clase figuraba
 * como "EmailNotificador" (duplicado por error de tipeo, ya que el ENUM
 * MedioComunicacion contempla WHATSAPP, EMAIL y SMS). Se corrige aquí el
 * nombre a SmsNotificador para reflejar correctamente su responsabilidad.
 *
 * Se simula la llamada al proveedor externo de SMS (por ejemplo, Twilio).
 */

@Component("smsNotificador")
public class SmsNotificador implements Notificador {

    @Override
    public void enviarNotificacion(Notificacion notificacion) {
        // integrar con proveedor real de envío de SMS (entrega futura).

    }


}
