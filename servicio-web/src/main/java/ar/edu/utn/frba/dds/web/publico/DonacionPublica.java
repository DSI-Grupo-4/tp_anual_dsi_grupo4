package ar.edu.utn.frba.dds.web.publico;

import java.time.LocalDateTime;

/**
 * Donación entregada, con solo los datos que se pueden mostrar sin login:
 * qué se donó, a qué entidad, cuándo y la foto de la entrega.
 * Nunca incluye dirección, camión ni datos del donante.
 *
 * @param categoria    nombre del enum (ALIMENTOS, ...), se traduce con messages "categoria.*"; puede ser null
 * @param subcategoria nombre del enum (ARROZ, ...), se traduce con "subcategoria.*"; puede ser null
 * @param cantidad     cantidad sin ceros de más ("20", "1.5"); puede ser null
 * @param unidad       nombre del enum (KILOGRAMO, ...), se traduce con "unidad.*"; puede ser null
 * @param fotoUrl      URL http(s) de una foto de la entrega, o null si no tiene
 */
public record DonacionPublica(
        String descripcion,
        String categoria,
        String subcategoria,
        String cantidad,
        String unidad,
        String entidad,
        LocalDateTime entregadaEn,
        String fotoUrl) {

    public boolean tieneFoto() {
        return fotoUrl != null;
    }
}
