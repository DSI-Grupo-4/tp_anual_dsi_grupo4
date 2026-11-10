package ar.edu.utn.frba.dds.web.navegacion;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Agrega al modelo de toda vista los datos que necesita el layout:
 * la ruta actual y, en las zonas privadas, el rol, el menú y las migas de pan.
 *
 * Por ahora el rol se deduce de la ruta (/admin, /donante, /beneficiaria).
 * Cuando exista autenticación (Persona 2), el rol sale del usuario autenticado
 * y el acceso a cada zona lo restringe Spring Security; el layout no cambia.
 */
@ControllerAdvice
public class NavegacionModelAdvice {

    private final NavegacionService navegacion;

    public NavegacionModelAdvice(NavegacionService navegacion) {
        this.navegacion = navegacion;
    }

    @ModelAttribute
    public void agregarNavegacion(HttpServletRequest request, Model model) {
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        model.addAttribute("rutaActual", ruta);
        String[] segmentos = ruta.split("/");
        if (segmentos.length < 2) {
            return;
        }
        Rol.desdeZona(segmentos[1]).ifPresent(rol -> {
            model.addAttribute("rolActual", rol);
            model.addAttribute("menu", navegacion.menu(rol));
            if (!model.containsAttribute("migas")) {
                model.addAttribute("migas", navegacion.migas(rol, ruta));
            }
        });
    }
}
