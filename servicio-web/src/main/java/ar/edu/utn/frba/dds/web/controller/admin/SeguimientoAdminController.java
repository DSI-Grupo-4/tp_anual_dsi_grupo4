package ar.edu.utn.frba.dds.web.controller.admin;

import ar.edu.utn.frba.dds.web.client.logistica.RutaApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutasClient;
import ar.edu.utn.frba.dds.web.error.BackendRechazoException;
import ar.edu.utn.frba.dds.web.logistica.NombresEntidades;
import ar.edu.utn.frba.dds.web.logistica.SeguimientoService;
import ar.edu.utn.frba.dds.web.mensajes.Toasts;
import ar.edu.utn.frba.dds.web.navegacion.Miga;
import java.util.List;
import java.util.Set;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Seguimiento de rutas y entregas de la administración (/admin/seguimiento): lista, detalle, iniciar y planificar. */
@Controller
@RequestMapping("/admin/seguimiento")
public class SeguimientoAdminController {

    private static final String LISTA = "/admin/seguimiento";
    private static final Set<String> ESTADOS = Set.of("PLANIFICADA", "INICIADA", "FINALIZADA", "CANCELADA");

    private final RutasClient rutas;
    private final SeguimientoService seguimiento;
    private final NombresEntidades nombres;
    private final MessageSource mensajes;

    public SeguimientoAdminController(RutasClient rutas, SeguimientoService seguimiento, NombresEntidades nombres,
                                      MessageSource mensajes) {
        this.rutas = rutas;
        this.seguimiento = seguimiento;
        this.nombres = nombres;
        this.mensajes = mensajes;
    }

    @GetMapping
    public String lista(@RequestParam(required = false) String estado, Model model) {
        String filtro = estado != null && ESTADOS.contains(estado) ? estado : null;
        List<RutaApi> todas = seguimiento.rutas(null);
        model.addAttribute("rutas", filtro == null ? todas : todas.stream().filter(r -> filtro.equals(r.estadoRuta())).toList());
        model.addAttribute("resumen", seguimiento.resumen(todas));
        model.addAttribute("filtro", filtro);
        return "admin/seguimiento";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Integer id, Model model) {
        model.addAttribute("ruta", rutas.ruta(id));
        model.addAttribute("nombres", nombres.porId());
        model.addAttribute("migas", List.of(
                Miga.clave("nav.admin.panel", "/admin"),
                Miga.clave("nav.admin.seguimiento", LISTA),
                Miga.texto(texto("logistica.seguimiento.ruta-n", id), null)));
        return "admin/seguimiento-detalle";
    }

    @PostMapping("/{id}/iniciar")
    public String iniciar(@PathVariable Integer id, RedirectAttributes attrs) {
        try {
            rutas.iniciar(id);
            Toasts.exito(attrs, texto("logistica.seguimiento.toast.iniciada", id));
        } catch (BackendRechazoException e) {
            // Por ejemplo, la ruta ya no está planificada: Logística lo explica.
            Toasts.error(attrs, e.getMessage());
        }
        return "redirect:" + LISTA + "/" + id;
    }

    @PostMapping("/planificar")
    public String planificar(RedirectAttributes attrs) {
        List<RutaApi> nuevas = rutas.planificar();
        if (nuevas == null || nuevas.isEmpty()) {
            Toasts.info(attrs, texto("logistica.seguimiento.toast.sin-pendientes"));
        } else {
            Toasts.exito(attrs, texto("logistica.seguimiento.toast.planificadas", nuevas.size()));
        }
        return "redirect:" + LISTA;
    }

    private String texto(String clave, Object... args) {
        return mensajes.getMessage(clave, args, LocaleContextHolder.getLocale());
    }
}
