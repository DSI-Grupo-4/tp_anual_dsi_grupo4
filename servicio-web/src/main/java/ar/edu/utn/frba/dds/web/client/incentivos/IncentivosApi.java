package ar.edu.utn.frba.dds.web.client.incentivos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDate;
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

    /** MisionDisponibleDTO. meta y objetivo pueden faltar si Incentivos es de una versión anterior. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Mision(String nombreMision, String categoriaNombre, String insigniaNombre,
                         BigDecimal progresoActual, BigDecimal distanciaRestante,
                         boolean completada, boolean activa, BigDecimal meta, String objetivo) {

        /** Lo que pide la misión: meta, o progreso + lo que falta si no vino. */
        public BigDecimal total() {
            if (meta != null) {
                return meta;
            }
            BigDecimal progreso = progresoActual == null ? BigDecimal.ZERO : progresoActual;
            return progreso.add(distanciaRestante == null ? BigDecimal.ZERO : distanciaRestante);
        }

        /** Porcentaje de avance, entre 0 y 100. */
        public int porcentaje() {
            if (completada) {
                return 100;
            }
            BigDecimal total = total();
            if (progresoActual == null || total.signum() <= 0) {
                return 0;
            }
            return Math.min(100, progresoActual.multiply(BigDecimal.valueOf(100))
                    .divide(total, 0, java.math.RoundingMode.DOWN).intValue());
        }
    }

    /** ProgresoInsigniaDTO: una insignia obtenida, visible u oculta. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InsigniaObtenida(String insigniaNombre, String imagenUrl, LocalDate fechaObtencion, boolean visible) {
    }

    /** InsigniaDTO: insignias visibles del donante. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Insignia(String nombre, String imagenUrl) {
    }

    /** MetricasSistemaDTO: totales de todo el sistema, para los reportes del admin. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MetricasSistema(int donantesActivos, int solicitudesDonacionTotales, int misionesCompletadasTotales,
                                  int insigniasOtorgadasTotales, List<EvolucionMes> evolucionMensual) {
        public List<EvolucionMes> evolucionMensual() {
            return evolucionMensual == null ? List.of() : evolucionMensual;
        }
    }

    /** RankingPosicionDTO: un puesto del ranking del mes en curso. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PosicionRanking(int puesto, Long donanteId, String donanteNombre, int cantidadMisiones, String mes) {
        public String nombre() {
            return donanteNombre == null || donanteNombre.isBlank() ? "Donante #" + donanteId : donanteNombre;
        }
    }

    /** RankingDTO: un ranking mensual ya cerrado (top 3). */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RankingMensual(LocalDate fechaEmision, List<PuestoRanking> topDonantes) {
        public List<PuestoRanking> topDonantes() {
            return topDonantes == null ? List.of() : topDonantes;
        }
    }

    /** ActividadMensualDonanteDTO: un puesto de un ranking cerrado. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PuestoRanking(Long donanteId, String donanteNombre, int cantidad, int puesto) {
        public String nombre() {
            return donanteNombre == null || donanteNombre.isBlank() ? "Donante #" + donanteId : donanteNombre;
        }
    }
}
