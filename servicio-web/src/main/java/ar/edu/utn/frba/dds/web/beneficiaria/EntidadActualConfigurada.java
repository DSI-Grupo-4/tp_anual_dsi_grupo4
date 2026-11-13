package ar.edu.utn.frba.dds.web.beneficiaria;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Collection;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * Usa la primera entidad vinculada en la sesión. El ID configurado es solo
 * para pruebas sin autenticación y con seguridad deshabilitada.
 */
@Component
public class EntidadActualConfigurada implements EntidadActual {

    private final Long id;
    @Value("${donatrack.web.auth.propagar-token:false}")
    private boolean seguridadHabilitada;

    public EntidadActualConfigurada(@Value("${donatrack.web.beneficiaria.entidad-id:1}") Long id) {
        this.id = id;
    }

    @Override
    public Long id() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof OidcUser usuario) {
            Object claim = usuario.getClaim("entidad_ids");
            var valores = claim instanceof Collection<?> lista ? lista
                    : claim == null ? java.util.List.of() : java.util.List.of(claim);
            for (Object valor : valores) {
                try {
                    long vinculada = Long.parseLong(String.valueOf(valor).trim());
                    if (vinculada > 0) return vinculada;
                } catch (NumberFormatException ignored) {
                    // Ignorar valores inválidos sin recurrir al ID de otra entidad.
                }
            }
            throw new AccessDeniedException("La cuenta no tiene una entidad vinculada. Volvé a ingresar o completá tus datos.");
        }
        if (seguridadHabilitada) {
            throw new AccessDeniedException("Se requiere una sesión vinculada a una entidad.");
        }
        return id;
    }
}
