package ar.edu.utn.frba.dds.web.controller.beneficiaria;

import ar.edu.utn.frba.dds.web.beneficiaria.EntidadActual;
import ar.edu.utn.frba.dds.web.beneficiaria.NecesidadForm;
import ar.edu.utn.frba.dds.web.beneficiaria.PortalBeneficiariaService;
import ar.edu.utn.frba.dds.web.client.donaciones.NecesidadApi;
import ar.edu.utn.frba.dds.web.client.donaciones.NecesidadesClient;
import ar.edu.utn.frba.dds.web.error.BackendRechazoException;
import ar.edu.utn.frba.dds.web.error.RecursoNoEncontradoException;
import ar.edu.utn.frba.dds.web.error.ServicioBackend;
import ar.edu.utn.frba.dds.web.mensajes.Toasts;
import ar.edu.utn.frba.dds.web.navegacion.Miga;
import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Necesidades de la entidad (/beneficiaria/necesidades): lista, alta, edición y baja.
 * Las recurrentes y las extraordinarias tienen formularios distintos (?tipo=), porque cada una pide otro dato.
 */
@Controller
@RequestMapping("/beneficiaria/necesidades")
public class NecesidadesController {

    private static final String LISTA = "/beneficiaria/necesidades";
    private static final String VISTA_LISTA = "beneficiaria/necesidades";
    private static final String VISTA_FORMULARIO = "beneficiaria/necesidad-formulario";

    private final EntidadActual entidadActual;
    private final NecesidadesClient necesidades;
    private final PortalBeneficiariaService portal;
    private final MessageSource mensajes;

    public NecesidadesController(EntidadActual entidadActual, NecesidadesClient necesidades,
                                 PortalBeneficiariaService portal, MessageSource mensajes) {
        this.entidadActual = entidadActual;
        this.necesidades = necesidades;
        this.portal = portal;
        this.mensajes = mensajes;
    }

    @GetMapping
    public String lista(Model model) {
        List<NecesidadApi> lista = necesidades.necesidades(entidadActual.id());
        model.addAttribute("necesidades", lista);
        model.addAttribute("resumen", portal.resumenNecesidades(lista));
        return VISTA_LISTA;
    }

    @GetMapping("/nueva")
    public String nueva(@RequestParam(defaultValue = NecesidadForm.RECURRENTE) String tipo, Model model) {
        NecesidadForm form = new NecesidadForm();
        form.setTipo(tipo);
        model.addAttribute("form", form);
        return formulario(model, null);
    }

    @PostMapping
    public String crear(@ModelAttribute("form") NecesidadForm form, BindingResult errores,
                        Model model, RedirectAttributes attrs) {
        validar(form, errores);
        if (!errores.hasErrors()) {
            try {
                necesidades.crear(entidadActual.id(), form.aPedido());
                Toasts.exito(attrs, texto("beneficiaria.necesidades.toast.creada"));
                return "redirect:" + LISTA;
            } catch (BackendRechazoException e) {
                e.volcarEn(errores);
            }
        }
        return formulario(model, null);
    }

    @GetMapping("/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("form", NecesidadForm.desde(buscar(id)));
        return formulario(model, id);
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @ModelAttribute("form") NecesidadForm form,
                             BindingResult errores, Model model, RedirectAttributes attrs) {
        validar(form, errores);
        if (!errores.hasErrors()) {
            try {
                necesidades.actualizar(entidadActual.id(), id, form.aPedido());
                Toasts.exito(attrs, texto("beneficiaria.necesidades.toast.actualizada"));
                return "redirect:" + LISTA;
            } catch (BackendRechazoException e) {
                e.volcarEn(errores);
            }
        }
        return formulario(model, id);
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes attrs) {
        try {
            necesidades.eliminar(entidadActual.id(), id);
            Toasts.exito(attrs, texto("beneficiaria.necesidades.toast.eliminada"));
        } catch (BackendRechazoException e) {
            // Por ejemplo, la necesidad ya tiene donaciones asignadas: se explica con el mensaje de Donaciones.
            Toasts.error(attrs, e.getMessage());
        }
        return "redirect:" + LISTA;
    }

    // ---- auxiliares ----

    private String formulario(Model model, Long necesidadId) {
        model.addAttribute("necesidadId", necesidadId);
        String miga = necesidadId == null ? "beneficiaria.necesidades.nueva" : "beneficiaria.necesidades.editar";
        model.addAttribute("migas", List.of(
                Miga.clave("nav.beneficiaria.panel", "/beneficiaria"),
                Miga.clave("nav.beneficiaria.necesidades", LISTA),
                Miga.clave(miga, null)));
        return VISTA_FORMULARIO;
    }

    private NecesidadApi buscar(Long id) {
        return necesidades.necesidades(entidadActual.id()).stream()
                .filter(n -> id.equals(n.id()))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException(ServicioBackend.DONACIONES,
                        "No existe la necesidad " + id + " de la entidad"));
    }

    /** Los obligatorios dependen del tipo. Los errores de conversión (número mal escrito) ya vienen en el BindingResult. */
    private void validar(NecesidadForm form, BindingResult errores) {
        exigir(errores, "descripcion", form.getDescripcion());
        exigir(errores, "subcategoria", form.getSubcategoria());
        exigir(errores, "unidadMedida", form.getUnidadMedida());
        if (form.esRecurrente()) {
            exigir(errores, "periodicidad", form.getPeriodicidad());
        } else {
            exigir(errores, "tipoExtraordinario", form.getTipoExtraordinario());
        }
        if (!errores.hasFieldErrors("cantidadRequerida")) {
            BigDecimal cantidad = form.getCantidadRequerida();
            if (cantidad == null) {
                errores.rejectValue("cantidadRequerida", "requerido", "Completá este campo.");
            } else if (cantidad.signum() <= 0) {
                errores.rejectValue("cantidadRequerida", "positivo", "Ingresá una cantidad mayor a 0.");
            }
        }
    }

    private static void exigir(BindingResult errores, String campo, String valor) {
        if (valor == null || valor.isBlank()) {
            errores.rejectValue(campo, "requerido", "Completá este campo.");
        }
    }

    private String texto(String clave) {
        return mensajes.getMessage(clave, null, LocaleContextHolder.getLocale());
    }
}
