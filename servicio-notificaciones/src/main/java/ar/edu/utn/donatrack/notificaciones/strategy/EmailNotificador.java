package ar.edu.utn.donatrack.notificaciones.strategy;

import ar.edu.utn.donatrack.notificaciones.model.Notificacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Envía la notificación por email a través de SMTP. En desarrollo el
 * servidor es Mailpit (bandeja de prueba local); en producción alcanza
 * con cambiar spring.mail.* para usar otro proveedor.
 */
@Component("emailNotificador")
public class EmailNotificador implements Notificador {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificador.class);

    private final JavaMailSender mailSender;
    private final String remitente;

    public EmailNotificador(JavaMailSender mailSender,
                            @Value("${notificaciones.email.remitente:no-responder@donatrack.local}") String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    @Override
    public void enviarNotificacion(Notificacion notificacion) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(remitente);
        mail.setTo(notificacion.getContacto());
        mail.setSubject(asunto(notificacion.getTipoEvento()));
        mail.setText(notificacion.getMensaje());

        // Si el servidor no responde, lanza MailException y el servicio marca la notificación como FALLIDA
        mailSender.send(mail);
        logger.info("Email enviado a {} ({})", notificacion.getContacto(), notificacion.getTipoEvento());
    }

    // "ENTREGA_CONFIRMADA" -> "DonaTrack - Entrega confirmada"
    private String asunto(String tipoEvento) {
        if (tipoEvento == null || tipoEvento.isBlank()) {
            return "DonaTrack - Notificación";
        }
        String texto = tipoEvento.replace('_', ' ').toLowerCase();
        return "DonaTrack - " + Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}