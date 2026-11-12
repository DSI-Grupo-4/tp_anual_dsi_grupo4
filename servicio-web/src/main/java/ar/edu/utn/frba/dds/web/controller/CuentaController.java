package ar.edu.utn.frba.dds.web.controller;

import ar.edu.utn.frba.dds.web.client.donaciones.DonacionesClient;
import ar.edu.utn.frba.dds.web.error.BackendRechazoException;
import ar.edu.utn.frba.dds.web.mensajes.Toasts;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Primer acceso de una cuenta que todavía no tiene rol de DonaTrack (registro propio
 * o ingreso con Google). Busca en Donaciones un donante o entidad con el mismo email
 * verificado; si lo encuentra, vuelve a pedir el login para traer el token con el rol nuevo.
 */
@Controller
public class CuentaController {

    private static final List<String> ZONAS = List.of("ADMIN", "DONANTE", "BENEFICIARIA");

    private final DonacionesClient donaciones;

    public CuentaController(DonacionesClient donaciones) {
        this.donaciones = donaciones;
    }

    @GetMapping("/cuenta")
    public String cuenta(Authentication auth, Model model) {
        List<String> roles = auth.getAuthorities().stream()
                .map(a -> a.getAuthority().replaceFirst("^ROLE_", ""))
                .filter(ZONAS::contains)
                .toList();
        model.addAttribute("roles", roles);
        return "cuenta/inicio";
    }

    @PostMapping("/cuenta/vincular")
    public String vincular(RedirectAttributes attrs) {
        DonacionesClient.Vinculacion vinculacion;
        try {
            vinculacion = donaciones.vincularCuenta();
        } catch (BackendRechazoException e) {
            Toasts.error(attrs, e.getMessage());
            return "redirect:/cuenta";
        }
        if (vinculacion == null || !vinculacion.vinculo()) {
            Toasts.advertencia(attrs, "No encontramos un donante ni una entidad con el correo de tu cuenta. "
                    + "Si te registraron en el depósito con otro correo, pedile a la administración que lo corrija.");
            return "redirect:/cuenta";
        }
        Toasts.exito(attrs, "Listo, tu cuenta quedó vinculada.");
        return "redirect:/oauth2/authorization/keycloak";
    }
}
