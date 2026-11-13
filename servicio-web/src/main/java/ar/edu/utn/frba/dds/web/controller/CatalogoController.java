package ar.edu.utn.frba.dds.web.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Catálogo de componentes del sistema de diseño: referencia para todo el equipo.
 * Solo existe con el perfil "dev" (--spring.profiles.active=dev).
 */
@Controller
@Profile("dev")
public class CatalogoController {

    @GetMapping("/dev/componentes")
    public String componentes() {
        return "dev/componentes";
    }
}
