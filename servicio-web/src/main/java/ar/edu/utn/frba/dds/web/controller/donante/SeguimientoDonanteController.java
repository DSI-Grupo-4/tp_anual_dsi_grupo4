package ar.edu.utn.frba.dds.web.controller.donante;

import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.DonacionDelDonante;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteClient;
import ar.edu.utn.frba.dds.web.donante.DonanteActual;
import ar.edu.utn.frba.dds.web.error.BackendException;
import ar.edu.utn.frba.dds.web.logistica.MarcadoresSeguimiento;
import ar.edu.utn.frba.dds.web.mapa.Marcador;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Seguimiento de las donaciones del donante (Persona 4): mapa con los camiones que llevan
 * sus donaciones en traslado, con los datos de ubicación de Logística (Persona 5,
 * contrato-ubicacion-para-persona4.md). Solo se ven los camiones; nunca las otras entregas.
 */
@Controller
public class SeguimientoDonanteController extends PortalDonanteBase {

    private static final Logger log = LoggerFactory.getLogger(SeguimientoDonanteController.class);
    /** Máximo que acepta GET /api/donantes/{id}/donaciones por página. */
    private static final int EN_TRASLADO_MAXIMO = 100;

    private final MarcadoresSeguimiento marcadores;

    public SeguimientoDonanteController(DonanteActual donanteActual, PortalDonanteClient donaciones,
                                        MarcadoresSeguimiento marcadores) {
        super(donanteActual, donaciones);
        this.marcadores = marcadores;
    }

    @GetMapping("/donante/seguimiento")
    public String seguimiento(Model model) {
        long id = donanteActual.id();
        Map<String, Long> porEstado = donaciones.resumenPorEstado(id);
        List<DonacionDelDonante> enTraslado = enTraslado(id);

        model.addAttribute("enTraslado", enTraslado);
        model.addAttribute("totalEnTraslado", porEstado.getOrDefault("EN_TRASLADO", 0L));
        model.addAttribute("porSalir", porEstado.getOrDefault("ASIGNACION_REALIZADA", 0L)
                + porEstado.getOrDefault("LISTA_PARA_ENTREGAR", 0L));
        model.addAttribute("entregadas", porEstado.getOrDefault("ENTREGADA", 0L));
        model.addAttribute("logisticaNoDisponible", false); // La vista lo niega: no puede quedar null.
        try {
            model.addAttribute("marcadores", marcadoresDe(enTraslado));
        } catch (BackendException e) {
            // Sin Logística, la lista de donaciones se muestra igual, con un aviso en lugar del mapa.
            log.warn("Seguimiento del donante {} sin datos de Logística: {}", id, e.getMessage());
            model.addAttribute("logisticaNoDisponible", true);
            model.addAttribute("marcadores", List.of());
        }
        return "donante/seguimiento";
    }

    /** Datos para refrescar el mapa. Si Logística no responde, 503: el mapa conserva lo último y avisa. */
    @GetMapping(value = "/donante/seguimiento/ubicaciones.json", produces = "application/json")
    @ResponseBody
    public ResponseEntity<List<Marcador>> ubicaciones() {
        try {
            return ResponseEntity.ok(marcadoresDe(enTraslado(donanteActual.id())));
        } catch (BackendException e) {
            log.debug("Refresco del mapa del donante sin datos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    private List<DonacionDelDonante> enTraslado(long donanteId) {
        return donaciones.donaciones(donanteId, Map.of(
                "estado", "EN_TRASLADO", "page", "0", "size", String.valueOf(EN_TRASLADO_MAXIMO))).content();
    }

    private List<Marcador> marcadoresDe(List<DonacionDelDonante> enTraslado) {
        if (enTraslado.isEmpty()) {
            return List.of(); // Nada en camino: no hace falta consultar Logística.
        }
        return marcadores.deCamiones(donaciones.camiones(donanteActual.id()));
    }
}
