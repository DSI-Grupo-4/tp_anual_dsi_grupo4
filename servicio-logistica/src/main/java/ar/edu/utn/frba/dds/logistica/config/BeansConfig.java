package ar.edu.utn.frba.dds.logistica.config;

import ar.edu.utn.frba.dds.logistica.domain.eventos.GestorEventos;
import ar.edu.utn.frba.dds.logistica.domain.rutas.GestorRutas;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfig {

    @Bean
    public GestorRutas gestorRutas() {
        return new GestorRutas();
    }

    @Bean
    public GestorEventos gestorEventos() {
        return new GestorEventos();
    }
}
