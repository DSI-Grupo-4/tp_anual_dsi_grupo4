package ar.edu.utn.frba.dds.web.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

/**
 * Páginas públicas (sin sesión). Las que todavía no están implementadas
 * muestran la vista "en construcción" con el layout público, así la
 * navegación funciona completa desde el principio.
 */
@Controller
public class PublicoController {

    /** Ruta pública → clave del título. Se van quitando a medida que cada página se implementa. */
    private static final Map<String, String> EN_CONSTRUCCION = Map.of(
            "galeria", "nav.galeria",
            "mapa", "nav.mapa",
            "ayuda", "nav.ayuda",
            "ingresar", "nav.ingresar",
            "registro", "nav.registrarse");

    private static final Map<String, String> LEGALES = Map.of(
            "privacidad", "pie.privacidad",
            "terminos", "pie.terminos");

    @GetMapping("/")
    public String inicio() {
        return "publico/inicio";
    }

    @GetMapping("/{pagina:galeria|mapa|ayuda|ingresar|registro}")
    public String enConstruccion(@PathVariable String pagina, Model model) {
        model.addAttribute("tituloClave", EN_CONSTRUCCION.get(pagina));
        return "publico/en-construccion";
    }

    @GetMapping("/legal/{pagina}")
    public String legal(@PathVariable String pagina, Model model) {
        String clave = LEGALES.get(pagina);
        if (clave == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        model.addAttribute("tituloClave", clave);
        return "publico/en-construccion";
    }
}
