package ar.edu.utn.frba.dds.logistica.seguridad;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Lectura del access token de Keycloak (misma regla que servicio-donaciones).
 * SERVICIO: token client_credentials de un cliente de la lista configurada.
 * entidad_ids: entidades que representa la cuenta (lo escribe servicio-donaciones).
 */
public final class UsuarioToken {

    public static final String ADMIN = "ADMIN";
    public static final String BENEFICIARIA = "BENEFICIARIA";
    public static final String SERVICIO = "SERVICIO";

    private static final Set<String> ROLES_KEYCLOAK = Set.of(ADMIN, "DONANTE", BENEFICIARIA);

    private UsuarioToken() {
    }

    static Converter<Jwt, AbstractAuthenticationToken> convertidor(Collection<String> clientesServicio) {
        return jwt -> new JwtAuthenticationToken(jwt, autoridades(jwt, clientesServicio),
                jwt.getClaimAsString("preferred_username"));
    }

    static List<GrantedAuthority> autoridades(Jwt jwt, Collection<String> clientesServicio) {
        List<GrantedAuthority> autoridades = new ArrayList<>();
        Map<String, Object> realm = jwt.getClaimAsMap("realm_access");
        if (realm != null && realm.get("roles") instanceof Collection<?> roles) {
            roles.stream().map(String::valueOf).filter(ROLES_KEYCLOAK::contains).distinct()
                    .forEach(rol -> autoridades.add(new SimpleGrantedAuthority("ROLE_" + rol)));
        }
        String cliente = jwt.getClaimAsString("azp");
        if (cliente != null && clientesServicio.contains(cliente)
                && ("service-account-" + cliente).equals(jwt.getClaimAsString("preferred_username"))) {
            autoridades.add(new SimpleGrantedAuthority("ROLE_" + SERVICIO));
        }
        return autoridades;
    }

    public static boolean tieneRol(Authentication auth, String rol) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> ("ROLE_" + rol).equals(a.getAuthority()));
    }

    /**
     * Entidades cuyas entregas puede confirmar quien llama. Una cuenta con token que no es ADMIN queda limitada a sus
     * {@code entidad_ids} (sin ninguna, no puede confirmar nada). {@code null} = sin límite: ADMIN, o sin token cuando
     * la seguridad está deshabilitada.
     */
    public static Set<Long> entidadesQuePuedeConfirmar(Authentication auth) {
        if (!(auth instanceof JwtAuthenticationToken) || tieneRol(auth, ADMIN)) {
            return null;
        }
        return entidadIds(auth);
    }

    public static Set<Long> entidadIds(Authentication auth) {
        Set<Long> ids = new LinkedHashSet<>();
        if (auth instanceof JwtAuthenticationToken token) {
            Object claim = token.getToken().getClaims().get("entidad_ids");
            if (claim instanceof Collection<?> valores) {
                valores.forEach(v -> agregar(ids, v));
            } else if (claim != null) {
                agregar(ids, claim);
            }
        }
        return ids;
    }

    private static void agregar(Set<Long> ids, Object valor) {
        try {
            ids.add(Long.parseLong(String.valueOf(valor).trim()));
        } catch (NumberFormatException e) {
            // Solo cuentan los ids numéricos que escribe servicio-donaciones.
        }
    }
}
