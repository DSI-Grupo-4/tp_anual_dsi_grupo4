package ar.edu.utn.frba.dds.web.error;

import java.util.Set;
import org.springframework.stereotype.Component;

/** Elige los textos de la página de error según el status (templates/error.html). */
@Component("paginasError")
public class PaginasError {

    private static final Set<Integer> CON_TEXTO_PROPIO = Set.of(400, 403, 404, 409, 413, 503);

    public int codigo(Integer status) {
        return status != null && CON_TEXTO_PROPIO.contains(status) ? status : 500;
    }

    public String titulo(Integer status) {
        return "error." + codigo(status) + ".titulo";
    }

    public String texto(Integer status) {
        return "error." + codigo(status) + ".texto";
    }
}
