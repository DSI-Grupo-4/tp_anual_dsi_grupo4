package ar.edu.utn.frba.dds.web.mapa;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Punto del componente de mapa (fragments/mapa.html + static/js/mapa.js). Es el contrato
 * entre las pantallas y el componente: el mapa no sabe de dónde salen los datos.
 *
 * <pre>
 * { "lat": -34.348, "lng": -58.795, "titulo": "Comedor Escobar Sonrisas",
 *   "detalle": "Arroz 1 kg × 20", "actualizadoEn": "2026-11-03T17:30Z", "tipo": "entrega" }
 * </pre>
 *
 * @param actualizadoEn fecha y hora ISO-8601 con zona (UTC), o null; el componente la muestra en hora local
 * @param tipo          "entrega" (donación entregada), "camion" (ubicación de un camión) o "sede" (sede de una entidad)
 */
public record Marcador(double lat, double lng, String titulo, String detalle, String actualizadoEn, String tipo) {

    public static final String ENTREGA = "entrega";
    public static final String CAMION = "camion";
    public static final String SEDE = "sede";

    /** Crea un marcador a partir de una fecha UTC sin zona, como la devuelven las APIs. */
    public static Marcador de(double lat, double lng, String titulo, String detalle, LocalDateTime actualizadoUtc, String tipo) {
        String iso = actualizadoUtc == null ? null : actualizadoUtc.atOffset(ZoneOffset.UTC).toString();
        return new Marcador(lat, lng, titulo, detalle, iso, tipo);
    }
}
