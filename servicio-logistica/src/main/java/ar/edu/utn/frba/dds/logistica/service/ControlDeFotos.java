package ar.edu.utn.frba.dds.logistica.service;

import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoEntrega;
import ar.edu.utn.frba.dds.logistica.repository.EntregaRepository;
import ar.edu.utn.frba.dds.logistica.seguridad.UsuarioToken;
import java.util.List;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Reglas de las fotos de entrega:
 * - Quien confirma una recepción solo puede usar fotos que subió él mismo y que todavía no están en ninguna entrega.
 * - Una foto es pública solo cuando está en una entrega ENTREGADA. Una subida sin confirmar nunca se publica.
 * Los administradores no pasan por el control de la subida. Sin token (seguridad deshabilitada) no se controla.
 */
@Service
public class ControlDeFotos {

    private static final String RUTA_FOTOS = "/api/fotos/";

    private final FotoService fotos;
    private final EntregaRepository entregas;

    public ControlDeFotos(FotoService fotos, EntregaRepository entregas) {
        this.fotos = fotos;
        this.entregas = entregas;
    }

    /**
     * @throws IllegalArgumentException si alguna URL no es de una foto subida a esta API (400)
     * @throws ResponseStatusException  403 si la foto la subió otra cuenta
     * @throws IllegalStateException    si la foto ya está asociada a una entrega (409)
     */
    public void exigirPropias(Authentication auth, List<String> urls) {
        if (!(auth instanceof JwtAuthenticationToken) || UsuarioToken.tieneRol(auth, UsuarioToken.ADMIN)) {
            return;
        }
        for (String url : urls) {
            String id = fotos.idDeUrl(url).orElseThrow(() ->
                    new IllegalArgumentException("Cada foto tiene que ser una de las que se subieron con POST /api/fotos"));
            boolean laSubioQuienConfirma = fotos.subidaPor(id).filter(auth.getName()::equals).isPresent();
            if (!laSubioQuienConfirma) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La foto no fue subida por esta cuenta");
            }
            if (entregas.existsByFotosUrl(url.trim())) {
                throw new IllegalStateException("La foto ya está asociada a una entrega");
            }
        }
    }

    /** Pública = la foto figura en una entrega ENTREGADA. Cualquier otro caso (sin confirmar, huérfana, inexistente) es falso. */
    public boolean esPublica(String id) {
        return id != null && ObjectId.isValid(id)
                && entregas.existsByEstadoEntregaAndFotosUrlEndingWith(EstadoEntrega.ENTREGADA, RUTA_FOTOS + id);
    }
}
