package ar.edu.utn.donatrack.notificaciones.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitNotificacionesConfig {

    public static final String EXCHANGE = "donatrack.notificaciones";
    public static final String QUEUE = "donatrack.notificaciones.solicitadas";
    public static final String ROUTING_KEY = "notificacion.solicitada";
    public static final String DEAD_LETTER_EXCHANGE = "donatrack.notificaciones.dlx";
    public static final String DEAD_LETTER_QUEUE = "donatrack.notificaciones.fallidas";

    @Bean
    DirectExchange notificacionesExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    Queue notificacionesQueue() {
        return QueueBuilder.durable(QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(ROUTING_KEY)
                .build();
    }

    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    Binding notificacionesBinding(Queue notificacionesQueue, DirectExchange notificacionesExchange) {
        return BindingBuilder.bind(notificacionesQueue).to(notificacionesExchange).with(ROUTING_KEY);
    }

    @Bean
    Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(ROUTING_KEY);
    }

    @Bean
    MessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
