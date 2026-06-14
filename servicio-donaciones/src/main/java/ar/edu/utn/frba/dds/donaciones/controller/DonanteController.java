package ar.edu.utn.frba.dds.donaciones.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/donantes")
public class DonanteController {

    @GetMapping
    public List<String> obtenerDonantes() {
        return List.of("Juan", "María");
    }
}