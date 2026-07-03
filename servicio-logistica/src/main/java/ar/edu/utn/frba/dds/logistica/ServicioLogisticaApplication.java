package ar.edu.utn.frba.dds.logistica;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ServicioLogisticaApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                ServicioLogisticaApplication.class,
                args
        );
    }
}
