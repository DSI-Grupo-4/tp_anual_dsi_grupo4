package ar.edu.utn.frba.dds.web.mensajes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Mensajes de resultado para el patrón POST-redirect-GET: el controller los agrega
 * antes de redirigir y el layout los muestra como toast en la página siguiente.
 *
 * <pre>
 * Toasts.exito(redirectAttributes, "La donación se registró correctamente.");
 * return "redirect:/admin/donaciones";
 * </pre>
 */
public final class Toasts {

    public static final String ATRIBUTO = "toasts";

    private Toasts() {
    }

    public static void exito(RedirectAttributes attrs, String texto) {
        agregar(attrs, new Toast(Toast.Tipo.EXITO, texto));
    }

    public static void error(RedirectAttributes attrs, String texto) {
        agregar(attrs, new Toast(Toast.Tipo.ERROR, texto));
    }

    public static void advertencia(RedirectAttributes attrs, String texto) {
        agregar(attrs, new Toast(Toast.Tipo.ADVERTENCIA, texto));
    }

    public static void info(RedirectAttributes attrs, String texto) {
        agregar(attrs, new Toast(Toast.Tipo.INFO, texto));
    }

    @SuppressWarnings("unchecked")
    private static void agregar(RedirectAttributes attrs, Toast toast) {
        Map<String, ?> flash = attrs.getFlashAttributes();
        List<Toast> lista = (List<Toast>) flash.get(ATRIBUTO);
        if (lista == null) {
            lista = new ArrayList<>();
            attrs.addFlashAttribute(ATRIBUTO, lista);
        }
        lista.add(toast);
    }
}
