package ar.edu.utn.frba.dds.donaciones.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServicioDonacionesController {

    @io.swagger.v3.oas.annotations.Operation(summary = "Ping", description = "Usar los IDs devueltos por las operaciones de alta. Los datos de prueba se mantienen en memoria.")
    @GetMapping("/ping")
    public String ping() {
        return "Servicio Donaciones OK";
    }
}
