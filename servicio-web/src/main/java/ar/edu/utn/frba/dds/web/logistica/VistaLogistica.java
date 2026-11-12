package ar.edu.utn.frba.dds.web.logistica;

import org.springframework.stereotype.Component;

/** Helpers de presentación de camiones, rutas y entregas. En las plantillas: ${@vistaLogistica.claseRuta(x)}. */
@Component("vistaLogistica")
public class VistaLogistica {

    /** Clase insignia-estado--X de una ruta. El texto del estado siempre se muestra: el color no es la única señal. */
    public String claseRuta(String estado) {
        if (estado == null) {
            return "";
        }
        return switch (estado) {
            case "PLANIFICADA" -> "insignia-estado--info";
            case "INICIADA" -> "insignia-estado--advertencia";
            case "FINALIZADA" -> "insignia-estado--exito";
            case "CANCELADA" -> "insignia-estado--error";
            default -> "";
        };
    }

    public String claseEntrega(String estado) {
        if (estado == null) {
            return "";
        }
        return switch (estado) {
            case "ENTREGADA" -> "insignia-estado--exito";
            case "EN_TRASLADO" -> "insignia-estado--advertencia";
            case "ASIGNADA_A_RUTA" -> "insignia-estado--marca";
            case "NO_RECIBIDA", "FALLIDA" -> "insignia-estado--error";
            case "PENDIENTE", "REPLANIFICABLE" -> "insignia-estado--info";
            default -> "";
        };
    }

    public String claseCamion(String estado) {
        if (estado == null) {
            return "";
        }
        return switch (estado) {
            case "DISPONIBLE" -> "insignia-estado--exito";
            case "ASIGNADO" -> "insignia-estado--marca";
            case "EN_RUTA" -> "insignia-estado--advertencia";
            case "FUERA_DE_SERVICIO" -> "insignia-estado--error";
            default -> "";
        };
    }

    /**
     * Devuelve la URL solo si es http o https; si no, null. Las fotos las carga cualquiera con una URL libre
     * (por ejemplo javascript:...), así que nunca se enlaza algo que no sea una dirección web.
     */
    public String urlSegura(String url) {
        if (url == null) {
            return null;
        }
        String limpia = url.trim().toLowerCase();
        return limpia.startsWith("http://") || limpia.startsWith("https://") ? url.trim() : null;
    }
}
