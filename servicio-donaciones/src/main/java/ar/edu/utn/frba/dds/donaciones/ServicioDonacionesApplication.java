package ar.edu.utn.frba.dds.donaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"ar.edu.utn.frba.dds.donaciones", "domain"})
@EnableScheduling
public class ServicioDonacionesApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServicioDonacionesApplication.class, args);
    }
}
