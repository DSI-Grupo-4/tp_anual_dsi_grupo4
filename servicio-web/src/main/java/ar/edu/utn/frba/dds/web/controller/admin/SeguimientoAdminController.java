package ar.edu.utn.frba.dds.web.controller.admin;

import ar.edu.utn.frba.dds.web.client.logistica.RutaApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutasClient;
import ar.edu.utn.frba.dds.web.client.logistica.SimulacionApi;
import ar.edu.utn.frba.dds.web.error.BackendRechazoException;
import ar.edu.utn.frba.dds.web.logistica.MarcadoresSeguimiento;
import ar.edu.utn.frba.dds.web.logistica.NombresEntidades;
import ar.edu.utn.frba.dds.web.logistica.SeguimientoService;
import ar.edu.utn.frba.dds.web.mapa.Marcador;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Seguimiento de rutas y entregas de la administración (/admin/seguimiento): lista, detalle, mapa,
 * iniciar, planificar y simular el avance del camión (solo demo).
 */
@Controller
@RequestMapping("/admin/seguimiento")
public class SeguimientoAdminController {

    private static final String LISTA = "/admin/seguimiento";
    private static final Set<String> ESTADOS = Set.of("PLANIFICADA", "INICIADA", "FINALIZADA", "CANCELADA");
    static final int LARGO_MAXIMO_MOTIVO = 300;

    private final RutasClient rutas;
    private final SeguimientoService seguimiento;
    private final MarcadoresSeguimiento marcadores;
    private final NombresEntidades nombres;
    private final MessageSource mensajes;

    public SeguimientoAdminController(RutasClient rutas, SeguimientoService seguimiento, MarcadoresSeguimiento marcadores,
                                      NombresEntidades nombres, MessageSource mensajes) {
        this.rutas = rutas;
        this.seguimiento = seguimiento;
        this.marcadores = marcadores;
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
        model.addAttribute("marcadores", marcadores.camionesEnCurso(todas));
        return "admin/seguimiento";
    }

    /** Datos para refrescar el mapa de la lista: los camiones de las rutas en curso. */
    @GetMapping(value = "/ubicaciones.json", produces = "application/json")
    @ResponseBody
    public List<Marcador> ubicaciones() {
        return marcadores.camionesEnCurso(rutas.rutas());
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Integer id, Model model) {
        RutaApi ruta = rutas.ruta(id);
        var nombresPorId = nombres.porId();
        model.addAttribute("ruta", ruta);
        model.addAttribute("nombres", nombresPorId);
        model.addAttribute("marcadores", marcadores.deRuta(ruta, nombresPorId));
        model.addAttribute("migas", List.of(
                Miga.clave("nav.admin.panel", "/admin"),
                Miga.clave("nav.admin.seguimiento", LISTA),
                Miga.texto(texto("logistica.seguimiento.ruta-n", id), null)));
        return "admin/seguimiento-detalle";
    }

    /** Datos para refrescar el mapa del detalle de una ruta. */
    @GetMapping(value = "/{id}/ubicaciones.json", produces = "application/json")
    @ResponseBody
    public List<Marcador> ubicacionesDeRuta(@PathVariable Integer id) {
        return marcadores.deRuta(rutas.ruta(id), nombres.porId());
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

    /** Simulador de la demo: mueve el camión un paso hacia la próxima parada. */
    @PostMapping("/{id}/simular")
    public String simular(@PathVariable Integer id, RedirectAttributes attrs) {
        try {
            SimulacionApi paso = rutas.simularAvance(id);
            if (paso.llegoAlDestino()) {
                Toasts.exito(attrs, texto("logistica.seguimiento.toast.simulado-llego", paso.numeroParadaDestino()));
            } else {
                Toasts.info(attrs, texto("logistica.seguimiento.toast.simulado", paso.numeroParadaDestino(), paso.distanciaRestanteKm()));
            }
        } catch (BackendRechazoException e) {
            // Por ejemplo, la ruta no está en curso o no hay paradas pendientes: Logística lo explica.
            Toasts.error(attrs, e.getMessage());
        }
        return "redirect:" + LISTA + "/" + id;
    }

    /** «No recibida»: nadie atendió en la parada. Solo afecta a las entregas que seguían en traslado. */
    @PostMapping("/{id}/paradas/{idParada}/no-recibida")
    public String noRecibida(@PathVariable Integer id, @PathVariable Integer idParada,
                             @RequestParam(defaultValue = "") String motivo, RedirectAttributes attrs) {
        String texto = motivo.trim();
        if (!motivoValido(texto, attrs)) {
            return "redirect:" + LISTA + "/" + id;
        }
        try {
            rutas.registrarNoRecibida(id, idParada, texto);
            Toasts.exito(attrs, texto("logistica.entregas.toast.no-recibida", idParada));
        } catch (BackendRechazoException e) {
            Toasts.error(attrs, e.getMessage());
        }
        return "redirect:" + LISTA + "/" + id;
    }

    /** Incidente: el camión no pudo entregar en la parada (calle cortada, avería). */
    @PostMapping("/{id}/paradas/{idParada}/incidente")
    public String incidente(@PathVariable Integer id, @PathVariable Integer idParada,
                            @RequestParam(defaultValue = "") String motivo, RedirectAttributes attrs) {
        String texto = motivo.trim();
        if (!motivoValido(texto, attrs)) {
            return "redirect:" + LISTA + "/" + id;
        }
        try {
            rutas.registrarIncidente(id, idParada, texto);
            Toasts.exito(attrs, texto("logistica.entregas.toast.incidente", idParada));
        } catch (BackendRechazoException e) {
            Toasts.error(attrs, e.getMessage());
        }
        return "redirect:" + LISTA + "/" + id;
    }

    /** El motivo es obligatorio y no puede ser desmedido; si no sirve se avisa con un toast y no se llama a Logística. */
    private boolean motivoValido(String motivo, RedirectAttributes attrs) {
        if (motivo.isEmpty()) {
            Toasts.error(attrs, texto("logistica.entregas.error.motivo-vacio"));
            return false;
        }
        if (motivo.length() > LARGO_MAXIMO_MOTIVO) {
            Toasts.error(attrs, texto("logistica.entregas.error.motivo-largo", LARGO_MAXIMO_MOTIVO));
            return false;
        }
        return true;
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
