package ar.edu.utn.frba.dds.web.incentivos;

import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.EvolucionMes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Filas del gráfico de evolución mensual de Incentivos (Persona 4). Lo usan el portal del
 * donante (/donante/incentivos) y los reportes del admin (/admin/reportes), con el
 * fragmento incentivos/fragmentos :: evolucion.
 */
public final class GraficoEvolucion {

    private GraficoEvolucion() {
    }

    /** Un mes del gráfico. porcentaje es relativo al mes con más donaciones. */
    public record Mes(LocalDate mes, int solicitudes, BigDecimal impacto, int porcentaje) {
    }

    /** Descarta los meses ilegibles (no cuentan para la escala) y calcula la altura de cada barra. */
    public static List<Mes> filas(List<EvolucionMes> meses) {
        List<EvolucionMes> validos = new ArrayList<>();
        List<LocalDate> inicios = new ArrayList<>();
        for (EvolucionMes mes : meses == null ? List.<EvolucionMes>of() : meses) {
            try {
                inicios.add(YearMonth.parse(mes.mes()).atDay(1));
                validos.add(mes);
            } catch (DateTimeParseException | NullPointerException e) {
                // Un mes ilegible no rompe el gráfico.
            }
        }
        int maximo = validos.stream().mapToInt(EvolucionMes::solicitudes).max().orElse(0);
        List<Mes> filas = new ArrayList<>();
        for (int i = 0; i < validos.size(); i++) {
            EvolucionMes mes = validos.get(i);
            int porcentaje = maximo == 0 ? 0 : Math.round(mes.solicitudes() * 100f / maximo);
            filas.add(new Mes(inicios.get(i), mes.solicitudes(),
                    mes.impacto() == null ? BigDecimal.ZERO : mes.impacto(), porcentaje));
        }
        return filas;
    }
}
