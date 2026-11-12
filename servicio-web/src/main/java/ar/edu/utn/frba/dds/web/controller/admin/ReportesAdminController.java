package ar.edu.utn.frba.dds.web.controller.admin;

import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.MetricasSistema;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.PosicionRanking;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.PuestoRanking;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.RankingMensual;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosClient;
import ar.edu.utn.frba.dds.web.error.BackendNoDisponibleException;
import ar.edu.utn.frba.dds.web.incentivos.GraficoEvolucion;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Reportes del admin (Persona 4): estadísticas de Incentivos, ranking del mes en curso
 * e historial de rankings mensuales.
 */
@Controller
public class ReportesAdminController {

    private static final Logger log = LoggerFactory.getLogger(ReportesAdminController.class);
    static final int PUESTOS_EN_CURSO = 10;

    private final IncentivosClient incentivos;

    public ReportesAdminController(IncentivosClient incentivos) {
        this.incentivos = incentivos;
    }

    /**
     * Un ranking cerrado. El cierre corre el día 1 (IncentivosScheduler), así que el ranking
     * corresponde al mes anterior a su fecha de emisión.
     */
    public record RankingDelMes(LocalDate mes, LocalDate emitido, List<PuestoRanking> puestos) {
    }

    @GetMapping("/admin/reportes")
    public String reportes(Model model) {
        MetricasSistema metricas;
        List<PosicionRanking> enCurso;
        List<RankingMensual> historial;
        try {
            metricas = incentivos.metricasSistema();
            enCurso = incentivos.rankingEnCurso();
            historial = incentivos.historialRankings();
        } catch (BackendNoDisponibleException e) {
            log.warn("Reportes sin datos de Incentivos: {}", e.getMessage());
            model.addAttribute("incentivosNoDisponible", true);
            return "admin/reportes";
        }

        model.addAttribute("metricas", metricas);
        model.addAttribute("evolucion", GraficoEvolucion.filas(metricas.evolucionMensual()));
        model.addAttribute("mesEnCurso", LocalDate.now().withDayOfMonth(1));
        model.addAttribute("rankingEnCurso", enCurso.stream().limit(PUESTOS_EN_CURSO).toList());
        model.addAttribute("totalEnCurso", enCurso.size());
        model.addAttribute("historial", historial(historial));
        return "admin/reportes";
    }

    /** Del más reciente al más viejo, con los puestos en orden. */
    public static List<RankingDelMes> historial(List<RankingMensual> rankings) {
        return rankings.stream()
                .filter(Objects::nonNull)
                .filter(r -> r.fechaEmision() != null)
                .sorted(Comparator.comparing(RankingMensual::fechaEmision).reversed())
                .map(r -> new RankingDelMes(r.fechaEmision().minusMonths(1).withDayOfMonth(1), r.fechaEmision(),
                        r.topDonantes().stream().sorted(Comparator.comparingInt(PuestoRanking::puesto)).toList()))
                .toList();
    }
}
