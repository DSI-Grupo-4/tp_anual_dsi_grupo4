package ar.edu.utn.frba.dds.web.controller.admin;

import ar.edu.utn.frba.dds.web.client.logistica.ChoferApi;
import ar.edu.utn.frba.dds.web.client.logistica.ChoferesClient;
import ar.edu.utn.frba.dds.web.error.BackendRechazoException;
import ar.edu.utn.frba.dds.web.error.RecursoNoEncontradoException;
import ar.edu.utn.frba.dds.web.error.ServicioBackend;
import ar.edu.utn.frba.dds.web.logistica.ChoferForm;
import ar.edu.utn.frba.dds.web.mensajes.Toasts;
import ar.edu.utn.frba.dds.web.navegacion.Miga;
import java.util.List;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** ABM de choferes de la administración (/admin/choferes). */
@Controller
@RequestMapping("/admin/choferes")
public class ChoferesAdminController {

    private static final String LISTA = "/admin/choferes";
    private static final String VISTA_LISTA = "admin/choferes";
    private static final String VISTA_FORMULARIO = "admin/chofer-formulario";

    private final ChoferesClient choferes;
    private final MessageSource mensajes;

    public ChoferesAdminController(ChoferesClient choferes, MessageSource mensajes) {
        this.choferes = choferes;
        this.mensajes = mensajes;
    }

    @GetMapping
    public String lista(Model model) {
        List<ChoferApi> todos = choferes.choferes();
        model.addAttribute("choferes", todos);
        model.addAttribute("total", todos.size());
        model.addAttribute("habilitados", todos.stream().filter(c -> Boolean.TRUE.equals(c.habilitado())).count());
        model.addAttribute("deshabilitados", todos.stream().filter(c -> !Boolean.TRUE.equals(c.habilitado())).count());
        return VISTA_LISTA;
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("form", new ChoferForm());
        return formulario(model, null);
    }

    @PostMapping
    public String crear(@ModelAttribute("form") ChoferForm form, BindingResult errores, Model model, RedirectAttributes attrs) {
        validar(form, errores);
        if (!errores.hasErrors()) {
            try {
                choferes.crear(form.aPedido());
                Toasts.exito(attrs, texto("logistica.choferes.toast.creado"));
                return "redirect:" + LISTA;
            } catch (BackendRechazoException e) {
                e.volcarEn(errores);
            }
        }
        return formulario(model, null);
    }

    @GetMapping("/{id}")
    public String editar(@PathVariable Integer id, Model model) {
        model.addAttribute("form", ChoferForm.desde(buscar(id)));
        return formulario(model, id);
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Integer id, @ModelAttribute("form") ChoferForm form, BindingResult errores,
                             Model model, RedirectAttributes attrs) {
        validar(form, errores);
        if (!errores.hasErrors()) {
            try {
                choferes.actualizar(id, form.aPedido());
                Toasts.exito(attrs, texto("logistica.choferes.toast.actualizado"));
                return "redirect:" + LISTA;
            } catch (BackendRechazoException e) {
                e.volcarEn(errores);
            }
        }
        return formulario(model, id);
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, RedirectAttributes attrs) {
        try {
            choferes.eliminar(id);
            Toasts.exito(attrs, texto("logistica.choferes.toast.eliminado"));
        } catch (BackendRechazoException e) {
            // Por ejemplo, el chofer tiene una ruta activa asignada: Logística lo explica.
            Toasts.error(attrs, e.getMessage());
        }
        return "redirect:" + LISTA;
    }

    // ---- auxiliares ----

    private String formulario(Model model, Integer choferId) {
        model.addAttribute("choferId", choferId);
        String miga = choferId == null ? "logistica.choferes.nuevo" : "logistica.choferes.editar";
        model.addAttribute("migas", List.of(
                Miga.clave("nav.admin.panel", "/admin"),
                Miga.clave("nav.admin.choferes", LISTA),
                Miga.clave(miga, null)));
        return VISTA_FORMULARIO;
    }

    private ChoferApi buscar(Integer id) {
        return choferes.choferes().stream()
                .filter(c -> id.equals(c.idChofer()))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException(ServicioBackend.LOGISTICA, "No existe el chofer " + id));
    }

    private void validar(ChoferForm form, BindingResult errores) {
        String nombre = form.getNombre() == null ? "" : form.getNombre().trim();
        if (nombre.isEmpty()) {
            errores.rejectValue("nombre", "requerido", "Completá este campo.");
        } else if (nombre.length() > 120) {
            errores.rejectValue("nombre", "largo", "El nombre es demasiado largo.");
        }
        if (errores.hasFieldErrors("dni")) {
            return;
        }
        if (form.getDni() == null) {
            errores.rejectValue("dni", "requerido", "Completá este campo.");
        } else if (form.getDni() <= 0) {
            errores.rejectValue("dni", "positivo", "Ingresá un número mayor a 0.");
        }
    }

    private String texto(String clave) {
        return mensajes.getMessage(clave, null, LocaleContextHolder.getLocale());
    }
}
