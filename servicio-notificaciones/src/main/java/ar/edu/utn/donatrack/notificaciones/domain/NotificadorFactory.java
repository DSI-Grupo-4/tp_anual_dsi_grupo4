package ar.edu.utn.donatrack.notificaciones.strategy;

import ar.edu.utn.donatrack.notificaciones.enums.MedioComunicacion;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * Fábrica encargada de resolver, en tiempo de ejecución, qué implementación
 * concreta de {@link Notificador} corresponde utilizar según el
 * {@link MedioComunicacion} solicitado.
 *
 dado el medio de una notificación, devuelve el
 * Notificador adecuado para procesarla.
 *
 * Al estar implementada con inyección de dependencias de Spring, agregar
 * un nuevo medio en el futuro solo requiere crear una nueva clase que
 * implemente Notificador y registrarla en el mapa, sin tocar el resto
 * del sistema
 */
@Component
public class NotificadorFactory {

    private final Map<MedioComunicacion, Notificador> notificadoresPorMedio;

    public NotificadorFactory(EmailNotificador emailNotificador,
                               SmsNotificador smsNotificador,
                               WhatsAppNotificador whatsAppNotificador) {
        this.notificadoresPorMedio = new EnumMap<>(MedioComunicacion.class);
        this.notificadoresPorMedio.put(MedioComunicacion.EMAIL, emailNotificador);
        this.notificadoresPorMedio.put(MedioComunicacion.SMS, smsNotificador);
        this.notificadoresPorMedio.put(MedioComunicacion.WHATSAPP, whatsAppNotificador);
    }

    /**
     * Obtiene el Notificador concreto correspondiente al medio indicado.
     *
     * @param medio medio de comunicación solicitado.
     * @return implementación de Notificador asociada a ese medio.
     * @throws IllegalArgumentException si el medio no tiene un Notificador registrado.
     */
    public Notificador obtenerNotificador(MedioComunicacion medio) {
        Notificador notificador = notificadoresPorMedio.get(medio);
        if (notificador == null) {
            throw new IllegalArgumentException("No existe un Notificador registrado para el medio: " + medio);
        }
        return notificador;
    }
}
