package ar.edu.utn.frba.dds.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ServicioWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServicioWebApplication.class, args);
    }
}
