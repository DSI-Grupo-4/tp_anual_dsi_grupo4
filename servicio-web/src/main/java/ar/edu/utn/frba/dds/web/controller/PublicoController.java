package ar.edu.utn.frba.dds.web.controller;

import ar.edu.utn.frba.dds.web.mapa.Marcador;
import ar.edu.utn.frba.dds.web.publico.FuentePublica;
import ar.edu.utn.frba.dds.web.publico.MapaPublico;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

/**
 * Páginas públicas (sin sesión): landing, galería, ayuda y legales.
 * Las que todavía no están implementadas muestran "en construcción" con el layout público.
 */
@Controller
public class PublicoController {

    static final int FOTOS_POR_PAGINA = 12;

    /** Ruta pública → clave del título. Se van quitando a medida que cada página se implementa. */
    private static final Map<String, String> EN_CONSTRUCCION = Map.of(
            "ingresar", "nav.ingresar",
            "registro", "nav.registrarse");

    private static final Set<String> LEGALES = Set.of("privacidad", "terminos");

    private final FuentePublica fuente;
    private final MapaPublico mapa;

    public PublicoController(FuentePublica fuente, MapaPublico mapa) {
        this.fuente = fuente;
        this.mapa = mapa;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("resumen", fuente.resumen());
        return "publico/inicio";
    }

    @GetMapping("/galeria")
    public String galeria(@RequestParam(defaultValue = "1") int pagina, Model model) {
        model.addAttribute("fotos", fuente.fotos(pagina, FOTOS_POR_PAGINA));
        return "publico/galeria";
    }

    @GetMapping("/mapa")
    public String mapa(Model model) {
        model.addAttribute("mapaEntregas", mapa.entregasRealizadas());
        return "publico/mapa";
    }

    /** Mismos marcadores en JSON: el componente de mapa los vuelve a pedir si tiene data-url e intervalo. */
    @GetMapping(value = "/mapa/entregas.json", produces = "application/json")
    @ResponseBody
    public List<Marcador> mapaDatos() {
        return mapa.entregasRealizadas().marcadores();
    }

    @GetMapping("/ayuda")
    public String ayuda() {
        return "publico/ayuda";
    }

    @GetMapping("/legal/{pagina}")
    public String legal(@PathVariable String pagina) {
        if (!LEGALES.contains(pagina)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return "publico/legal/" + pagina;
    }

    @GetMapping("/{pagina:ingresar|registro}")
    public String enConstruccion(@PathVariable String pagina, Model model) {
        model.addAttribute("tituloClave", EN_CONSTRUCCION.get(pagina));
        return "publico/en-construccion";
    }
}
