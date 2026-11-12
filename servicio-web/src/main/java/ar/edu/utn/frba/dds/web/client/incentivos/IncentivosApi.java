package ar.edu.utn.frba.dds.web.client.incentivos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;

/** Respuestas del servicio de Incentivos. Solo los campos que usa el cliente web. */
public final class IncentivosApi {

    private IncentivosApi() {
    }

    /** Períodos que acepta GET /api/donantes/{id}/metricas. */
    public static final List<String> PERIODOS = List.of("HISTORICO", "MENSUAL", "TRIMESTRAL", "SEMESTRAL", "ANUAL");

    /** MetricasActividadDTO. categoriaActual es null si todavía no completó ninguna misión. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Metricas(String donanteNombre, String categoriaActual, String periodo,
                           int solicitudesDonacionHechas, int beneficiariosAyudados, int misionesCompletadas,
                           int insigniasObtenidas, BigDecimal impactoAcumulado, Integer posicionRanking,
                           List<EvolucionMes> evolucionMensual) {
        public List<EvolucionMes> evolucionMensual() {
            return evolucionMensual == null ? List.of() : evolucionMensual;
        }
    }

    /** EvolucionMensualDTO. mes llega como "2026-11". */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EvolucionMes(String mes, int solicitudes, BigDecimal impacto) {
    }

    /** MisionDisponibleDTO. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Mision(String nombreMision, String categoriaNombre, String insigniaNombre,
                         BigDecimal progresoActual, BigDecimal distanciaRestante,
                         boolean completada, boolean activa) {
    }

    /** InsigniaDTO: insignias visibles del donante. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Insignia(String nombre, String imagenUrl) {
    }
}
