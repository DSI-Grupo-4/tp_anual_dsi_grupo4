package ar.edu.utn.frba.dds.web.controller;

import ar.edu.utn.frba.dds.web.navegacion.NavegacionService;
import ar.edu.utn.frba.dds.web.navegacion.Rol;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

/**
 * Respaldo para las secciones privadas que todavía no tienen controller propio:
 * muestra "en construcción" con el layout privado y el menú del rol.
 *
 * Cada persona implementa su sección con un controller propio mapeado a la ruta
 * literal (por ejemplo @GetMapping("/admin/donaciones")). Spring prioriza la ruta
 * literal sobre este patrón con variables, así que no hace falta tocar esta clase.
 */
@Controller
public class SeccionesPrivadasController {

    private final NavegacionService navegacion;

    public SeccionesPrivadasController(NavegacionService navegacion) {
        this.navegacion = navegacion;
    }

    @GetMapping({"/{zona:admin|donante|beneficiaria}", "/{zona:admin|donante|beneficiaria}/{seccion}"})
    public String enConstruccion(@PathVariable String zona, @PathVariable(required = false) String seccion) {
        Rol rol = Rol.desdeZona(zona).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (seccion != null && !navegacion.existeSeccion(rol, seccion)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return "privado/en-construccion";
    }
}
