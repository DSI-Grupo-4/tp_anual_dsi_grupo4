package ar.edu.utn.frba.dds.incentivos.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/incentivos")
public class ServicioIncentivosController {
    @GetMapping("/ping")
    public String ping() {
        return "incentivos OK";
    }
}
