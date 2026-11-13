package ar.edu.utn.frba.dds.web.error;

import java.util.Map;
import org.springframework.validation.BindingResult;

/**
 * El backend rechazó la operación por una regla de negocio o datos inválidos
 * (400, 409, 413, 422). Trae el mensaje general y, si el backend los mandó,
 * los errores por campo ({"error": "...", "detalles": {"campo": "mensaje"}}).
 *
 * Uso típico en un controller con formulario:
 * <pre>
 * try {
 *     cliente.crear(form);
 * } catch (BackendRechazoException e) {
 *     e.volcarEn(bindingResult);
 *     return "admin/donantes/formulario";
 * }
 * </pre>
 */
public class BackendRechazoException extends BackendException {

    private final Map<String, String> erroresPorCampo;

    public BackendRechazoException(ServicioBackend servicio, int status, String mensaje, Map<String, String> erroresPorCampo) {
        super(servicio, status, mensaje, null);
        this.erroresPorCampo = Map.copyOf(erroresPorCampo);
    }

    public Map<String, String> getErroresPorCampo() {
        return erroresPorCampo;
    }

    /**
     * Copia los errores en el BindingResult del formulario: los de campos que existen
     * en el formulario van a ese campo; el mensaje general queda como error global.
     */
    public void volcarEn(BindingResult resultado) {
        erroresPorCampo.forEach((campo, mensaje) -> {
            if (resultado.getTarget() != null && esCampoDelFormulario(resultado, campo)) {
                resultado.rejectValue(campo, "backend", mensaje);
            } else {
                resultado.reject("backend", campo + ": " + mensaje);
            }
        });
        if (erroresPorCampo.isEmpty()) {
            resultado.reject("backend", getMessage());
        }
    }

    private static boolean esCampoDelFormulario(BindingResult resultado, String campo) {
        try {
            return resultado.getFieldType(campo) != null;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
