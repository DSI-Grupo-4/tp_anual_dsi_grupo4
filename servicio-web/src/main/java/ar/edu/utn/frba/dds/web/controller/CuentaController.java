package ar.edu.utn.frba.dds.web.controller;

import ar.edu.utn.frba.dds.web.client.donaciones.DonacionesClient;
import ar.edu.utn.frba.dds.web.cuenta.AltaDonanteForm;
import ar.edu.utn.frba.dds.web.cuenta.AltaDonanteJuridicoForm;
import ar.edu.utn.frba.dds.web.cuenta.AltaEntidadForm;
import ar.edu.utn.frba.dds.web.cuenta.NuevoRepresentanteForm;
import ar.edu.utn.frba.dds.web.error.BackendException;
import ar.edu.utn.frba.dds.web.error.BackendRechazoException;
import ar.edu.utn.frba.dds.web.error.SesionVencidaException;
import ar.edu.utn.frba.dds.web.mensajes.Toasts;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Primer acceso y autorregistro. Si el correo verificado ya está en un donante o una
 * entidad, "Buscar mis datos" lo vincula. Si no existe, la persona completa su ficha
 * o da de alta la entidad de la que es el primer representante.
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
        completar(auth, model);
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
                    + "Si te registraron en el depósito con otro correo, pedile a la administración que lo corrija. "
                    + "Si todavía no estás cargado, completá el formulario de esta página.");
            return "redirect:/cuenta";
        }
        Toasts.exito(attrs, "Listo, tu cuenta quedó vinculada.");
        return "redirect:/oauth2/authorization/keycloak";
    }

    @PostMapping("/cuenta/donante")
    public String registrarDonante(@ModelAttribute("donanteForm") AltaDonanteForm form, BindingResult errores,
                                   Authentication auth, Model model, RedirectAttributes attrs) {
        if (roles(auth).contains("DONANTE")) {
            Toasts.error(attrs, "Tu cuenta ya está vinculada a un donante.");
            return "redirect:/cuenta";
        }
        String email = emailVerificado(auth).orElse(null);
        if (email == null) {
            Toasts.error(attrs, "Tu correo todavía no está verificado. Revisá el mail y volvé a ingresar.");
            return "redirect:/cuenta";
        }
        form.validar(errores);
        if (errores.hasErrors()) {
            completar(auth, model);
            return "cuenta/inicio";
        }
        try {
            donaciones.registrarDonante(form.datos(email));
        } catch (BackendRechazoException e) {
            errores.reject("api", e.getMessage());
            completar(auth, model);
            return "cuenta/inicio";
        }
        Toasts.exito(attrs, "Listo, tus datos quedaron cargados. Volvé a ingresar para usar tu perfil.");
        return "redirect:/oauth2/authorization/keycloak";
    }

    @PostMapping("/cuenta/donante-juridico")
    public String registrarDonanteJuridico(@ModelAttribute("donanteJuridicoForm") AltaDonanteJuridicoForm form,
                                           BindingResult errores, Authentication auth, Model model,
                                           RedirectAttributes attrs) {
        if (roles(auth).contains("DONANTE")) {
            Toasts.error(attrs, "Tu cuenta ya está vinculada a un donante.");
            return "redirect:/cuenta";
        }
        String email = emailVerificado(auth).orElse(null);
        if (email == null) {
            Toasts.error(attrs, "Tu correo todavía no está verificado. Revisá el mail y volvé a ingresar.");
            return "redirect:/cuenta";
        }
        form.validar(errores);
        if (errores.hasErrors()) {
            completar(auth, model);
            return "cuenta/inicio";
        }
        try {
            donaciones.registrarDonanteJuridico(form.datos(email));
        } catch (BackendRechazoException e) {
            errores.reject("api", e.getMessage());
            completar(auth, model);
            return "cuenta/inicio";
        }
        Toasts.exito(attrs, "La organización quedó registrada. Volvé a ingresar para usar tu perfil.");
        return "redirect:/oauth2/authorization/keycloak";
    }

    @PostMapping("/cuenta/entidad")
    public String registrarEntidad(@ModelAttribute("entidadForm") AltaEntidadForm form, BindingResult errores,
                                   Authentication auth, Model model, RedirectAttributes attrs) {
        if (roles(auth).contains("BENEFICIARIA")) {
            Toasts.error(attrs, "Tu cuenta ya representa a una entidad.");
            return "redirect:/cuenta";
        }
        String email = emailVerificado(auth).orElse(null);
        if (email == null) {
            Toasts.error(attrs, "Tu correo todavía no está verificado. Revisá el mail y volvé a ingresar.");
            return "redirect:/cuenta";
        }
        form.validar(errores);
        if (errores.hasErrors()) {
            completar(auth, model);
            return "cuenta/inicio";
        }
        try {
            donaciones.registrarEntidad(form.datos(email));
        } catch (BackendRechazoException e) {
            errores.reject("api", e.getMessage());
            completar(auth, model);
            return "cuenta/inicio";
        }
        Toasts.exito(attrs, "La entidad quedó registrada. Volvé a ingresar para verla en tu perfil.");
        return "redirect:/oauth2/authorization/keycloak";
    }

    @PostMapping("/cuenta/representantes")
    public String agregarRepresentante(@ModelAttribute("representanteForm") NuevoRepresentanteForm form,
                                       BindingResult errores, Authentication auth, Model model, RedirectAttributes attrs) {
        List<String> roles = roles(auth);
        if (!roles.contains("BENEFICIARIA") && !roles.contains("ADMIN")) {
            Toasts.error(attrs, "Solo un representante de la entidad o un administrador puede sumar a otra persona.");
            return "redirect:/cuenta";
        }
        form.validar(errores);
        List<Long> propias = idsDeEntidades(auth);
        if (form.getEntidadId() != null && !roles.contains("ADMIN") && !propias.contains(form.getEntidadId())) {
            errores.rejectValue("entidadId", "dato", "Esa entidad no está vinculada a tu cuenta.");
        }
        if (errores.hasErrors()) {
            completar(auth, model);
            return "cuenta/inicio";
        }
        try {
            donaciones.agregarRepresentante(form.getEntidadId(), form.datos());
        } catch (BackendRechazoException e) {
            errores.reject("api", e.getMessage());
            completar(auth, model);
            return "cuenta/inicio";
        }
        Toasts.exito(attrs, "Listo. Si esa persona ya verificó su correo, entra con su cuenta. Si no, le llega el mail para elegir contraseña.");
        return "redirect:/cuenta";
    }

    private void completar(Authentication auth, Model model) {
        List<String> roles = roles(auth);
        model.addAttribute("roles", roles);
        model.addAttribute("emailCuenta", emailVerificado(auth).orElse(null));
        model.addAttribute("entidadesPropias", nombres(idsDeEntidades(auth)));
        if (!model.containsAttribute("donanteForm")) {
            model.addAttribute("donanteForm", new AltaDonanteForm());
        }
        if (!model.containsAttribute("donanteJuridicoForm")) {
            model.addAttribute("donanteJuridicoForm", new AltaDonanteJuridicoForm());
        }
        if (!model.containsAttribute("entidadForm")) {
            model.addAttribute("entidadForm", new AltaEntidadForm());
        }
        if (!model.containsAttribute("representanteForm")) {
            NuevoRepresentanteForm representante = new NuevoRepresentanteForm();
            List<Long> ids = idsDeEntidades(auth);
            if (ids.size() == 1) {
                representante.setEntidadId(ids.get(0));
            }
            model.addAttribute("representanteForm", representante);
        }
    }

    private List<EntidadPropia> nombres(List<Long> ids) {
        List<EntidadPropia> salida = new ArrayList<>();
        for (Long id : ids) {
            String nombre = "Entidad " + id;
            try {
                var entidad = donaciones.entidad(id);
                if (entidad != null && entidad.nombre() != null) {
                    nombre = entidad.nombre();
                }
            } catch (SesionVencidaException e) {
                throw e;
            } catch (BackendException e) {
                nombre = "Entidad " + id;
            }
            salida.add(new EntidadPropia(id, nombre));
        }
        return salida;
    }

    private static List<String> roles(Authentication auth) {
        if (auth == null) {
            return List.of();
        }
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority().replaceFirst("^ROLE_", ""))
                .filter(ZONAS::contains)
                .toList();
    }

    private static Optional<String> emailVerificado(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof OidcUser usuario
                && Boolean.TRUE.equals(usuario.getEmailVerified())
                && usuario.getEmail() != null && !usuario.getEmail().isBlank()) {
            return Optional.of(usuario.getEmail().trim().toLowerCase());
        }
        return Optional.empty();
    }

    private static List<Long> idsDeEntidades(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof OidcUser usuario)) {
            return List.of();
        }
        Object claim = usuario.getClaim("entidad_ids");
        List<Long> ids = new ArrayList<>();
        if (claim instanceof Collection<?> valores) {
            valores.forEach(valor -> numero(valor).ifPresent(ids::add));
        } else {
            numero(claim).ifPresent(ids::add);
        }
        return ids;
    }

    private static Optional<Long> numero(Object valor) {
        if (valor == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(String.valueOf(valor).trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public record EntidadPropia(Long id, String nombre) {
    }
}
