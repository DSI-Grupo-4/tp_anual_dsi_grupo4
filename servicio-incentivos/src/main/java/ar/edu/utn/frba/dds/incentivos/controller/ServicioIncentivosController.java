package ar.edu.utn.frba.dds.incentivos.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/incentivos")
public class ServicioIncentivosController {

    @GetMapping("/ping")
    public String ping() {
        return "incentivos OK";
    }
}
