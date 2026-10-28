package ar.edu.utn.frba.dds.logistica.config;

import ar.edu.utn.frba.dds.logistica.domain.eventos.GestorEventos;
import ar.edu.utn.frba.dds.logistica.domain.rutas.GestorRutas;
import ar.edu.utn.frba.dds.logistica.repository.EventoLogisticoRepository;
import ar.edu.utn.frba.dds.logistica.repository.RutaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfig {

    // Los gestores siguen siendo clases de dominio sin anotaciones de Spring; acá se les inyecta su repositorio.
    @Bean
    public GestorRutas gestorRutas(RutaRepository rutaRepository) {
        return new GestorRutas(rutaRepository);
    }

    @Bean
    public GestorEventos gestorEventos(EventoLogisticoRepository eventoLogisticoRepository) {
        return new GestorEventos(eventoLogisticoRepository);
    }
}
