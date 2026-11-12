package ar.edu.utn.frba.dds.web.donante;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 * Textos y formatos de las vistas del portal del donante (Persona 4).
 * Las etiquetas de estado viven acá y no en messages.properties para no generar
 * conflictos en ese archivo compartido; se pueden mover cuando el equipo lo unifique.
 */
@Component("donanteVista")
public class DonanteVista {

    private static final Locale ES_AR = Locale.forLanguageTag("es-AR");

    /** Estados de EstadoTrack en el orden del circuito, con su etiqueta. */
    public static final Map<String, String> ESTADOS;

    static {
        Map<String, String> estados = new LinkedHashMap<>();
        estados.put("EN_DEPOSITO", "En depósito");
        estados.put("ASIGNACION_REALIZADA", "Asignada");
        estados.put("LISTA_PARA_ENTREGAR", "Lista para entregar");
        estados.put("EN_TRASLADO", "En traslado");
        estados.put("ENTREGADA", "Entregada");
        estados.put("ENTREGA_FALLIDA", "Entrega fallida");
        estados.put("VENCIDA", "Vencida");
        ESTADOS = java.util.Collections.unmodifiableMap(estados);
    }

    /**
     * Subcategorías de cada categoría, como las valida Donaciones (ItemDonado).
     * Se usan para agrupar el filtro de subcategoría y para descartar combinaciones imposibles.
     */
    public static final Map<String, List<String>> SUBCATEGORIAS = Map.of(
            "ALIMENTOS", List.of("FIDEOS_SECOS", "ARROZ", "LEGUMBRES_SECAS", "ACEITE_VEGETAL", "TOMATE", "FRUTA"),
            "MOBILIARIO", List.of("SILLA", "MESA", "BANCO", "COLCHON"),
            "VESTIMENTA", List.of("CAMPERA", "REMERA", "PANTALON", "ROPA_INFANTIL", "FRAZADA"));

    public static final List<String> CATEGORIAS = List.of("ALIMENTOS", "MOBILIARIO", "VESTIMENTA");

    /** Estados en los que la donación ya salió del depósito y todavía no llegó. */
    public static final List<String> EN_CAMINO = List.of("ASIGNACION_REALIZADA", "LISTA_PARA_ENTREGAR", "EN_TRASLADO");

    private final MessageSource mensajes;

    public DonanteVista(MessageSource mensajes) {
        this.mensajes = mensajes;
    }

    public String estado(String estado) {
        if (estado == null) {
            return "Sin informar";
        }
        return ESTADOS.getOrDefault(estado, estado);
    }

    /** Modificador de .insignia-estado según el estado (mismos colores que el catálogo de componentes). */
    public String claseEstado(String estado) {
        if (estado == null) {
            return "insignia-estado";
        }
        return switch (estado) {
            case "EN_DEPOSITO" -> "insignia-estado insignia-estado--info";
            case "ASIGNACION_REALIZADA", "LISTA_PARA_ENTREGAR" -> "insignia-estado insignia-estado--marca";
            case "EN_TRASLADO" -> "insignia-estado insignia-estado--advertencia";
            case "ENTREGADA" -> "insignia-estado insignia-estado--exito";
            case "ENTREGA_FALLIDA" -> "insignia-estado insignia-estado--error";
            default -> "insignia-estado";
        };
    }

    /** Etiqueta de categoría o subcategoría de bienes, desde messages.properties (categoria.X / subcategoria.X). */
    public String bien(String prefijo, String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return "Sin informar";
        }
        return mensajes.getMessage(prefijo + "." + codigo, null, codigo, ES_AR);
    }

    /** Categoría a la que pertenece una subcategoría, o null si no es una subcategoría conocida. */
    public static String categoriaDe(String subcategoria) {
        if (subcategoria == null) {
            return null;
        }
        return SUBCATEGORIAS.entrySet().stream()
                .filter(e -> e.getValue().contains(subcategoria))
                .map(Map.Entry::getKey).findFirst().orElse(null);
    }

    /** Condición de un bien (NUEVO, USADO…), legible. */
    public String condicion(String condicion) {
        if (condicion == null || condicion.isBlank()) {
            return null;
        }
        String texto = condicion.toLowerCase(ES_AR).replace('_', ' ');
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    /** Cantidad con formato argentino, sin decimales innecesarios: 1.250 o 2,5. */
    public String numero(BigDecimal valor) {
        if (valor == null) {
            return "0";
        }
        NumberFormat formato = NumberFormat.getNumberInstance(ES_AR);
        formato.setMaximumFractionDigits(2);
        return formato.format(valor.setScale(2, RoundingMode.HALF_UP));
    }
}
