package ar.edu.utn.frba.dds.web.controller.admin;

import ar.edu.utn.frba.dds.web.client.logistica.EntregaAdminApi;
import ar.edu.utn.frba.dds.web.client.logistica.EntregasClient;
import ar.edu.utn.frba.dds.web.error.BackendRechazoException;
import ar.edu.utn.frba.dds.web.logistica.NombresEntidades;
import ar.edu.utn.frba.dds.web.logistica.SeguimientoService;
import ar.edu.utn.frba.dds.web.mensajes.Toasts;
import ar.edu.utn.frba.dds.web.navegacion.Miga;
import java.util.Comparator;
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

/**
 * Entregas para la administración (/admin/seguimiento/entregas): listado con filtro por estado, y reingreso al depósito
 * de las que no se pudieron concretar para que el planificador las tome de nuevo.
 */
@Controller
@RequestMapping("/admin/seguimiento/entregas")
public class EntregasAdminController {

    private static final String LISTA = "/admin/seguimiento/entregas";
    private static final Set<String> ESTADOS = Set.of(
            "PENDIENTE", "ASIGNADA_A_RUTA", "EN_TRASLADO", "ENTREGADA", "NO_RECIBIDA", "FALLIDA", "REPLANIFICABLE");

    private final EntregasClient entregas;
    private final SeguimientoService seguimiento;
    private final NombresEntidades nombres;
    private final MessageSource mensajes;

    public EntregasAdminController(EntregasClient entregas, SeguimientoService seguimiento, NombresEntidades nombres,
                                   MessageSource mensajes) {
        this.entregas = entregas;
        this.seguimiento = seguimiento;
        this.nombres = nombres;
        this.mensajes = mensajes;
    }

    @GetMapping
    public String lista(@RequestParam(required = false) String estado, Model model) {
        String filtro = estado != null && ESTADOS.contains(estado) ? estado : null;
        List<EntregaAdminApi> todas = entregas.entregas().stream()
                .sorted(Comparator.comparing(EntregaAdminApi::idEntrega, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        model.addAttribute("entregas", filtro == null ? todas : todas.stream().filter(e -> filtro.equals(e.estadoEntrega())).toList());
        model.addAttribute("resumen", seguimiento.resumenEntregas(todas));
        model.addAttribute("filtro", filtro);
        model.addAttribute("nombres", nombres.porId());
        model.addAttribute("migas", List.of(
                Miga.clave("nav.admin.panel", "/admin"),
                Miga.clave("nav.admin.seguimiento", "/admin/seguimiento"),
                Miga.clave("logistica.entregas.titulo", null)));
        return "admin/entregas";
    }

    @PostMapping("/{id}/reingresar")
    public String reingresar(@PathVariable Integer id, RedirectAttributes attrs) {
        try {
            entregas.reingresar(id);
            Toasts.exito(attrs, texto("logistica.entregas.toast.reingresada", id));
        } catch (BackendRechazoException e) {
            // Por ejemplo, la entrega ya no estaba no recibida ni fallida: Logística lo explica.
            Toasts.error(attrs, e.getMessage());
        }
        return "redirect:" + LISTA;
    }

    private String texto(String clave, Object... argumentos) {
        return mensajes.getMessage(clave, argumentos, LocaleContextHolder.getLocale());
    }
}
