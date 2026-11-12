package ar.edu.utn.frba.dds.web.controller.admin;

import ar.edu.utn.frba.dds.web.client.logistica.CamionApi;
import ar.edu.utn.frba.dds.web.client.logistica.CamionesClient;
import ar.edu.utn.frba.dds.web.error.BackendRechazoException;
import ar.edu.utn.frba.dds.web.error.RecursoNoEncontradoException;
import ar.edu.utn.frba.dds.web.error.ServicioBackend;
import ar.edu.utn.frba.dds.web.logistica.CamionForm;
import ar.edu.utn.frba.dds.web.logistica.SeguimientoService;
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

/** ABM de camiones de la administración (/admin/camiones). */
@Controller
@RequestMapping("/admin/camiones")
public class CamionesAdminController {

    private static final String LISTA = "/admin/camiones";
    private static final String VISTA_LISTA = "admin/camiones";
    private static final String VISTA_FORMULARIO = "admin/camion-formulario";
    private static final int LARGO_MAXIMO_PATENTE = 12;

    private final CamionesClient camiones;
    private final SeguimientoService seguimiento;
    private final MessageSource mensajes;

    public CamionesAdminController(CamionesClient camiones, SeguimientoService seguimiento, MessageSource mensajes) {
        this.camiones = camiones;
        this.seguimiento = seguimiento;
        this.mensajes = mensajes;
    }

    @GetMapping
    public String lista(Model model) {
        List<CamionApi> flota = camiones.camiones();
        model.addAttribute("camiones", flota);
        model.addAttribute("resumen", seguimiento.resumenFlota(flota));
        return VISTA_LISTA;
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        CamionForm form = new CamionForm();
        form.setEstadoCamion("DISPONIBLE");
        model.addAttribute("form", form);
        return formulario(model, null, true);
    }

    @PostMapping
    public String crear(@ModelAttribute("form") CamionForm form, BindingResult errores, Model model, RedirectAttributes attrs) {
        validar(form, errores);
        if (!errores.hasErrors()) {
            try {
                camiones.crear(form.aPedido(true));
                Toasts.exito(attrs, texto("logistica.camiones.toast.creado"));
                return "redirect:" + LISTA;
            } catch (BackendRechazoException e) {
                e.volcarEn(errores);
            }
        }
        return formulario(model, null, true);
    }

    @GetMapping("/{id}")
    public String editar(@PathVariable Integer id, Model model) {
        CamionApi camion = buscar(id);
        model.addAttribute("form", CamionForm.desde(camion));
        model.addAttribute("estadoActual", camion.estadoCamion());
        return formulario(model, id, camion.estadoEditable());
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Integer id, @ModelAttribute("form") CamionForm form, BindingResult errores,
                             Model model, RedirectAttributes attrs) {
        CamionApi actual = buscar(id);
        validar(form, errores);
        if (!errores.hasErrors()) {
            try {
                camiones.actualizar(id, form.aPedido(actual.estadoEditable()));
                Toasts.exito(attrs, texto("logistica.camiones.toast.actualizado"));
                return "redirect:" + LISTA;
            } catch (BackendRechazoException e) {
                e.volcarEn(errores);
            }
        }
        model.addAttribute("estadoActual", actual.estadoCamion());
        return formulario(model, id, actual.estadoEditable());
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, RedirectAttributes attrs) {
        try {
            camiones.eliminar(id);
            Toasts.exito(attrs, texto("logistica.camiones.toast.eliminado"));
        } catch (BackendRechazoException e) {
            // Por ejemplo, el camión tiene entregas activas o figura en rutas anteriores: Logística lo explica.
            Toasts.error(attrs, e.getMessage());
        }
        return "redirect:" + LISTA;
    }

    // ---- auxiliares ----

    private String formulario(Model model, Integer camionId, boolean estadoEditable) {
        model.addAttribute("camionId", camionId);
        model.addAttribute("estadoEditable", estadoEditable);
        String miga = camionId == null ? "logistica.camiones.nuevo" : "logistica.camiones.editar";
        model.addAttribute("migas", List.of(
                Miga.clave("nav.admin.panel", "/admin"),
                Miga.clave("nav.admin.camiones", LISTA),
                Miga.clave(miga, null)));
        return VISTA_FORMULARIO;
    }

    private CamionApi buscar(Integer id) {
        return camiones.camiones().stream()
                .filter(c -> id.equals(c.idCamion()))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException(ServicioBackend.LOGISTICA, "No existe el camión " + id));
    }

    private void validar(CamionForm form, BindingResult errores) {
        String patente = form.getPatente() == null ? "" : form.getPatente().replaceAll("\\s+", "");
        if (patente.isEmpty()) {
            errores.rejectValue("patente", "requerido", "Completá este campo.");
        } else if (patente.length() > LARGO_MAXIMO_PATENTE) {
            errores.rejectValue("patente", "largo", "La patente es demasiado larga.");
        }
        entero(errores, "capacidadCargaKg", form.getCapacidadCargaKg());
        entero(errores, "capacidadVolumenM3", form.getCapacidadVolumenM3());
        entero(errores, "alturaM", form.getAlturaM());
    }

    /** Un número escrito mal ya viene como error de conversión en el BindingResult. */
    private static void entero(BindingResult errores, String campo, Integer valor) {
        if (errores.hasFieldErrors(campo)) {
            return;
        }
        if (valor == null) {
            errores.rejectValue(campo, "requerido", "Completá este campo.");
        } else if (valor <= 0) {
            errores.rejectValue(campo, "positivo", "Ingresá un número mayor a 0.");
        }
    }

    private String texto(String clave) {
        return mensajes.getMessage(clave, null, LocaleContextHolder.getLocale());
    }
}
