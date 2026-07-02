package ar.edu.utn.frba.dds.incentivos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ServicioIncentivosApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                ServicioIncentivosApplication.class,
                args
        );
    }
}
