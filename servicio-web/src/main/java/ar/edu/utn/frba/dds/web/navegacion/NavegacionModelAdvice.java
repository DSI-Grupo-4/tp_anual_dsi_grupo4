package ar.edu.utn.frba.dds.web.navegacion;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Agrega al modelo de toda vista los datos que necesita el layout:
 * la ruta actual, el usuario autenticado y, en las zonas privadas, el rol, el menú y las migas.
 * El rol sale de las autoridades de la sesión (ROLE_ADMIN, ROLE_DONANTE, ROLE_BENEFICIARIA).
 * Quién puede entrar a cada zona lo decide SeguridadConfig.
 */
@ControllerAdvice
public class NavegacionModelAdvice {

    private final NavegacionService navegacion;

    public NavegacionModelAdvice(NavegacionService navegacion) {
        this.navegacion = navegacion;
    }

    @ModelAttribute
    public void agregarNavegacion(HttpServletRequest request, Model model, Authentication autenticacion) {
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        model.addAttribute("rutaActual", ruta);
        model.addAttribute("sesionIniciada", autenticacion != null && autenticacion.isAuthenticated()
                && !(autenticacion instanceof AnonymousAuthenticationToken));
        if (autenticacion != null && autenticacion.getPrincipal() instanceof OidcUser usuario) {
            String nombre = nombreDe(usuario);
            if (!nombre.isBlank()) {
                model.addAttribute("usuarioActual", new UsuarioActual(nombre));
            }
        }
        String[] segmentos = ruta.split("/");
        if (segmentos.length < 2) {
            return;
        }
        Optional<Rol> rolZona = Rol.desdeZona(segmentos[1]);
        if (rolZona.isEmpty()) {
            return;
        }
        Optional<Rol> rolUsuario = rolDe(autenticacion);
        if (rolUsuario.isPresent() && rolUsuario.get() != rolZona.get()) {
            return;
        }
        Rol rol = rolUsuario.orElse(rolZona.get());
        model.addAttribute("rolActual", rol);
        model.addAttribute("menu", navegacion.menu(rol));
        if (!model.containsAttribute("migas")) {
            model.addAttribute("migas", navegacion.migas(rol, ruta));
        }
    }

    private static String nombreDe(OidcUser usuario) {
        String nombre = usuario.getFullName();
        if (nombre == null || nombre.isBlank()) {
            nombre = usuario.getPreferredUsername();
        }
        return nombre == null ? "" : nombre;
    }

    private static Optional<Rol> rolDe(Authentication autenticacion) {
        if (autenticacion == null || autenticacion instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        for (Rol rol : Rol.values()) {
            String autoridad = "ROLE_" + rol.name();
            for (GrantedAuthority otorgada : autenticacion.getAuthorities()) {
                if (autoridad.equals(otorgada.getAuthority())) {
                    return Optional.of(rol);
                }
            }
        }
        return Optional.empty();
    }
}
