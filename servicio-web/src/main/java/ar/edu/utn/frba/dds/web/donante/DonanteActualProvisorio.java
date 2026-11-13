package ar.edu.utn.frba.dds.web.donante;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * Usa el donante vinculado en la sesión OIDC. Sin autenticación y con seguridad
 * deshabilitada, permite el ID de pruebas DONANTE_PROVISORIO_ID.
 */
@Component
public class DonanteActualProvisorio implements DonanteActual {

    private final long id;
    @Value("${donatrack.web.auth.propagar-token:false}")
    private boolean seguridadHabilitada;

    public DonanteActualProvisorio(@Value("${donatrack.web.donante-provisorio-id:${DONANTE_PROVISORIO_ID:1}}") long id) {
        this.id = id;
    }

    @Override
    public long id() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof OidcUser usuario) {
            Object claim = usuario.getClaim("donante_id");
            try {
                long vinculado = Long.parseLong(String.valueOf(claim));
                if (vinculado > 0) return vinculado;
            } catch (NumberFormatException ignored) {
                // Una sesión sin vínculo nunca debe usar el donante de pruebas.
            }
            throw new AccessDeniedException("La cuenta no tiene un donante vinculado. Volvé a ingresar o completá tus datos.");
        }
        if (seguridadHabilitada) {
            throw new AccessDeniedException("Se requiere una sesión vinculada a un donante.");
        }
        return id;
    }
}
