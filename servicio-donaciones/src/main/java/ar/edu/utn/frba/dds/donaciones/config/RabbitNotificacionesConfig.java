package ar.edu.utn.frba.dds.donaciones.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitNotificacionesConfig {

    public static final String EXCHANGE = "donatrack.notificaciones";
    public static final String ROUTING_KEY = "notificacion.solicitada";

    @Bean
    DirectExchange notificacionesExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    MessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
