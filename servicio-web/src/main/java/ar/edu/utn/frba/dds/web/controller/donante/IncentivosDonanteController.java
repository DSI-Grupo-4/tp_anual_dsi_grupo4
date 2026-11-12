package ar.edu.utn.frba.dds.web.controller.donante;

import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Perfil;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteClient;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.EvolucionMes;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.InsigniaObtenida;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.Metricas;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.Mision;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosClient;
import ar.edu.utn.frba.dds.web.donante.DonanteActual;
import ar.edu.utn.frba.dds.web.error.BackendNoDisponibleException;
import ar.edu.utn.frba.dds.web.error.BackendRechazoException;
import ar.edu.utn.frba.dds.web.error.RecursoNoEncontradoException;
import ar.edu.utn.frba.dds.web.mensajes.Toasts;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Incentivos del donante (Persona 4): métricas por período, evolución mensual, misiones,
 * categoría e insignias con su visibilidad. Solo para personas humanas (requerimiento 7).
 */
@Controller
@RequestMapping("/donante/incentivos")
public class IncentivosDonanteController extends PortalDonanteBase {

    private static final Logger log = LoggerFactory.getLogger(IncentivosDonanteController.class);
    private static final String RUTA = "/donante/incentivos";

    /** Categorías de Incentivos en orden (GestorMisiones). */
    static final List<String> CATEGORIAS = List.of("Colaborador", "Sostenedor", "Transformador");

    static final Map<String, String> ETIQUETAS_PERIODO = Map.of(
            "HISTORICO", "Histórico", "MENSUAL", "Este mes", "TRIMESTRAL", "Últimos 3 meses",
            "SEMESTRAL", "Últimos 6 meses", "ANUAL", "Último año");

    private static final List<String> PESTANAS = List.of("misiones", "insignias");

    private final IncentivosClient incentivos;

    public IncentivosDonanteController(DonanteActual donanteActual, PortalDonanteClient donaciones,
                                       IncentivosClient incentivos) {
        super(donanteActual, donaciones);
        this.incentivos = incentivos;
    }

    /** Un mes del gráfico de evolución. porcentaje es relativo al mes con más donaciones. */
    public record MesEvolucion(LocalDate mes, int solicitudes, BigDecimal impacto, int porcentaje) {
    }

    /** Un escalón de la escalera de categorías. */
    public record Escalon(String nombre, boolean alcanzada, boolean actual) {
    }

    /** Opción del selector de período. */
    public record OpcionPeriodo(String valor, String etiqueta, String enlace, boolean elegida) {
    }

    @GetMapping
    public String incentivos(@ModelAttribute(name = "perfil", binding = false) Perfil perfil,
                             @RequestParam(required = false) String periodo,
                             @RequestParam(required = false) String pestana,
                             Model model, RedirectAttributes flash) {
        if (!perfil.esHumana()) {
            Toasts.info(flash, "Incentivos está disponible para donantes personas humanas.");
            return "redirect:/donante";
        }
        long id = donanteActual.id();
        // List.of no acepta contains(null): se compara solo si vino el parámetro.
        String periodoElegido = periodo != null && IncentivosApi.PERIODOS.contains(periodo) ? periodo : "HISTORICO";
        model.addAttribute("pestana", pestana != null && PESTANAS.contains(pestana) ? pestana : "misiones");
        model.addAttribute("periodos", opcionesPeriodo(periodoElegido));
        model.addAttribute("periodoElegido", ETIQUETAS_PERIODO.get(periodoElegido));

        Metricas metricas;
        List<Mision> misiones;
        List<InsigniaObtenida> insignias;
        try {
            metricas = incentivos.metricas(id, periodoElegido);
            misiones = incentivos.misiones(id);
            insignias = incentivos.insigniasObtenidas(id);
        } catch (BackendNoDisponibleException e) {
            log.warn("Incentivos del donante {} no disponible: {}", id, e.getMessage());
            model.addAttribute("incentivosNoDisponible", true);
            return "donante/incentivos";
        }

        model.addAttribute("metricas", metricas);
        model.addAttribute("misiones", misiones);
        model.addAttribute("misionesCompletadas", misiones.stream().filter(Mision::completada).count());
        model.addAttribute("misionActiva", misiones.stream().filter(Mision::activa).findFirst().orElse(null));
        model.addAttribute("insignias", insignias);
        model.addAttribute("evolucion", evolucion(metricas.evolucionMensual()));
        model.addAttribute("escalera", escalera(metricas.categoriaActual()));
        model.addAttribute("siguienteCategoria", siguienteCategoria(metricas.categoriaActual()));
        return "donante/incentivos";
    }

    @PostMapping("/insignias/visibilidad")
    public String cambiarVisibilidad(@ModelAttribute(name = "perfil", binding = false) Perfil perfil,
                                     @RequestParam String insignia, @RequestParam boolean visible,
                                     RedirectAttributes flash) {
        if (!perfil.esHumana()) {
            return "redirect:/donante";
        }
        try {
            incentivos.cambiarVisibilidad(donanteActual.id(), insignia, visible);
            Toasts.exito(flash, visible ? "La insignia «" + insignia + "» ahora es visible."
                    : "Ocultaste la insignia «" + insignia + "».");
        } catch (RecursoNoEncontradoException | BackendRechazoException e) {
            Toasts.error(flash, "No pudimos cambiar la visibilidad de «" + insignia + "». Recargá la página y probá de nuevo.");
        } catch (BackendNoDisponibleException e) {
            Toasts.error(flash, "Incentivos no está disponible en este momento. Probá de nuevo en unos minutos.");
        }
        return "redirect:" + RUTA + "?pestana=insignias";
    }

    private static List<OpcionPeriodo> opcionesPeriodo(String elegido) {
        return IncentivosApi.PERIODOS.stream().map(valor -> new OpcionPeriodo(valor, ETIQUETAS_PERIODO.get(valor),
                UriComponentsBuilder.fromPath(RUTA).queryParam("periodo", valor).build().toUriString(),
                valor.equals(elegido))).toList();
    }

    static List<MesEvolucion> evolucion(List<EvolucionMes> meses) {
        // Primero se descartan los meses ilegibles, así no cuentan para la escala.
        List<EvolucionMes> validos = new ArrayList<>();
        List<LocalDate> inicios = new ArrayList<>();
        for (EvolucionMes mes : meses) {
            try {
                inicios.add(YearMonth.parse(mes.mes()).atDay(1));
                validos.add(mes);
            } catch (DateTimeParseException | NullPointerException e) {
                log.debug("Mes ilegible en la evolución de Incentivos: {}", mes.mes());
            }
        }
        int maximo = validos.stream().mapToInt(EvolucionMes::solicitudes).max().orElse(0);
        List<MesEvolucion> filas = new ArrayList<>();
        for (int i = 0; i < validos.size(); i++) {
            EvolucionMes mes = validos.get(i);
            int porcentaje = maximo == 0 ? 0 : Math.round(mes.solicitudes() * 100f / maximo);
            filas.add(new MesEvolucion(inicios.get(i), mes.solicitudes(),
                    mes.impacto() == null ? BigDecimal.ZERO : mes.impacto(), porcentaje));
        }
        return filas;
    }

    static List<Escalon> escalera(String actual) {
        int indice = actual == null ? -1 : CATEGORIAS.indexOf(actual);
        List<Escalon> escalones = new ArrayList<>();
        for (int i = 0; i < CATEGORIAS.size(); i++) {
            escalones.add(new Escalon(CATEGORIAS.get(i), i <= indice, i == indice));
        }
        return escalones;
    }

    static String siguienteCategoria(String actual) {
        int indice = actual == null ? -1 : CATEGORIAS.indexOf(actual);
        return indice + 1 < CATEGORIAS.size() ? CATEGORIAS.get(indice + 1) : null;
    }
}
