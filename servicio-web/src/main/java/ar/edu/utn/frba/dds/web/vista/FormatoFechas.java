package ar.edu.utn.frba.dds.web.vista;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Formatea fechas para las vistas. Las APIs devuelven fechas y horas en UTC
 * sin zona (D-037); acá se convierten a la hora local configurada en
 * donatrack.web.zona-horaria. Uso en Thymeleaf:
 * <pre>
 *   th:text="${@fechas.fechaHora(entrega.fechaHoraEntrega)}"   → 03/11/2026 14:30
 *   th:text="${@fechas.fecha(donacion.fechaVencimiento)}"      → 03/11/2026
 *   th:datetime="${@fechas.iso(entrega.fechaHoraEntrega)}"     → para &lt;time datetime&gt;
 * </pre>
 */
@Component("fechas")
public class FormatoFechas {

    private static final String SIN_DATO = "—";
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("es-AR"));
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-AR"));
    private static final DateTimeFormatter MES = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", Locale.forLanguageTag("es-AR"));

    private final ZoneId zona;

    public FormatoFechas(@Value("${donatrack.web.zona-horaria}") String zona) {
        this.zona = ZoneId.of(zona);
    }

    /** Fecha y hora UTC sin zona (LocalDateTime de las APIs) → hora local. */
    public String fechaHora(LocalDateTime utc) {
        return utc == null ? SIN_DATO : FECHA_HORA.format(utc.atOffset(ZoneOffset.UTC).atZoneSameInstant(zona));
    }

    public String fechaHora(Instant instante) {
        return instante == null ? SIN_DATO : FECHA_HORA.format(instante.atZone(zona));
    }

    public String fechaHora(OffsetDateTime fechaHora) {
        return fechaHora == null ? SIN_DATO : FECHA_HORA.format(fechaHora.atZoneSameInstant(zona));
    }

    /** Fechas sin hora (vencimientos, fecha de lote): no se convierten de zona. */
    public String fecha(LocalDate fecha) {
        return fecha == null ? SIN_DATO : FECHA.format(fecha);
    }

    /** Fecha y hora UTC → fecha local (sin hora). */
    public String fecha(LocalDateTime utc) {
        return utc == null ? SIN_DATO : FECHA.format(utc.atOffset(ZoneOffset.UTC).atZoneSameInstant(zona));
    }

    /** "noviembre de 2026" */
    public String mes(LocalDate fecha) {
        return fecha == null ? SIN_DATO : MES.format(fecha);
    }

    /** Valor ISO 8601 con zona, para el atributo datetime de &lt;time&gt;. */
    public String iso(LocalDateTime utc) {
        return utc == null ? null : utc.atOffset(ZoneOffset.UTC).toString();
    }

    public ZoneId zona() {
        return zona;
    }
}
