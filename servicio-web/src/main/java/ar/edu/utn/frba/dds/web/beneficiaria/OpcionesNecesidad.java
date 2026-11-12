package ar.edu.utn.frba.dds.web.beneficiaria;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Opciones de los selectores del formulario de necesidades. Los valores son los enums de Donaciones;
 * las etiquetas visibles salen de messages (categoria.X, subcategoria.X, unidad.X, periodicidad.X, extraordinario.X).
 * Las plantillas la usan como ${@opcionesNecesidad.subcategoriasPorCategoria()}.
 */
@Component("opcionesNecesidad")
public class OpcionesNecesidad {

    private static final Map<String, List<String>> SUBCATEGORIAS = new LinkedHashMap<>();
    static {
        SUBCATEGORIAS.put("ALIMENTOS", List.of("FIDEOS_SECOS", "ARROZ", "LEGUMBRES_SECAS", "ACEITE_VEGETAL", "TOMATE", "FRUTA"));
        SUBCATEGORIAS.put("MOBILIARIO", List.of("SILLA", "MESA", "BANCO", "COLCHON"));
        SUBCATEGORIAS.put("VESTIMENTA", List.of("CAMPERA", "REMERA", "PANTALON", "ROPA_INFANTIL", "FRAZADA"));
    }

    public Map<String, List<String>> subcategoriasPorCategoria() {
        return SUBCATEGORIAS;
    }

    public List<String> unidades() {
        return List.of("UNIDAD", "KILOGRAMO", "CAJA", "PAQUETE", "LITRO");
    }

    public List<String> periodicidades() {
        return List.of("DIARIA", "SEMANAL", "MENSUAL", "ANUAL");
    }

    public List<String> tiposExtraordinarios() {
        return List.of("INUNDACION", "SISMO", "OLA_POLAR");
    }
}
