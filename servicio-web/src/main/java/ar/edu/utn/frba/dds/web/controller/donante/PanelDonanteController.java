package ar.edu.utn.frba.dds.web.controller.donante;

import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Perfil;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteClient;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.Metricas;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosClient;
import ar.edu.utn.frba.dds.web.donante.DonanteActual;
import ar.edu.utn.frba.dds.web.donante.DonanteVista;
import ar.edu.utn.frba.dds.web.error.BackendException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Panel del donante: resumen de sus donaciones y de sus incentivos (Persona 4). */
@Controller
public class PanelDonanteController {

    private static final Logger log = LoggerFactory.getLogger(PanelDonanteController.class);
    static final int RECIENTES = 5;

    private final DonanteActual donanteActual;
    private final PortalDonanteClient donaciones;
    private final IncentivosClient incentivos;

    public PanelDonanteController(DonanteActual donanteActual, PortalDonanteClient donaciones, IncentivosClient incentivos) {
        this.donanteActual = donanteActual;
        this.donaciones = donaciones;
        this.incentivos = incentivos;
    }

    /** Una fila del gráfico de distribución por estado. */
    public record CantidadPorEstado(String estado, long cantidad, long porcentaje) {
    }

    @GetMapping("/donante")
    public String panel(Model model) {
        long id = donanteActual.id();
        Perfil perfil = donaciones.perfil(id);
        Map<String, Long> porEstado = donaciones.resumenPorEstado(id);

        long total = porEstado.values().stream().mapToLong(Long::longValue).sum();
        long enCamino = DonanteVista.EN_CAMINO.stream().mapToLong(e -> porEstado.getOrDefault(e, 0L)).sum();
        List<CantidadPorEstado> distribucion = new ArrayList<>();
        DonanteVista.ESTADOS.keySet().forEach(estado -> {
            long cantidad = porEstado.getOrDefault(estado, 0L);
            if (cantidad > 0) {
                distribucion.add(new CantidadPorEstado(estado, cantidad, Math.round(cantidad * 100.0 / total)));
            }
        });

        model.addAttribute("perfil", perfil);
        model.addAttribute("total", total);
        model.addAttribute("enCamino", enCamino);
        model.addAttribute("entregadas", porEstado.getOrDefault("ENTREGADA", 0L));
        model.addAttribute("distribucion", distribucion);
        model.addAttribute("cantidadRecientes", RECIENTES);
        model.addAttribute("recientes", total == 0 ? List.of()
                : donaciones.donaciones(id, Map.of("page", "0", "size", String.valueOf(RECIENTES))).content());

        // Incentivos es solo para personas humanas (requerimiento 7). Si el servicio no responde,
        // el panel se muestra igual, con un aviso en lugar de las métricas.
        if (perfil.esHumana()) {
            try {
                Metricas metricas = incentivos.metricas(id, "HISTORICO");
                model.addAttribute("metricas", metricas);
            } catch (BackendException e) {
                log.warn("Panel del donante {} sin métricas de Incentivos: {}", id, e.getMessage());
                model.addAttribute("incentivosNoDisponible", true);
            }
        }
        return "donante/panel";
    }
}
