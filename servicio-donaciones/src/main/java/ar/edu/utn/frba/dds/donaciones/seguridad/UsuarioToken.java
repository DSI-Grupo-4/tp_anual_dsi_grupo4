package ar.edu.utn.frba.dds.donaciones.seguridad;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * Lectura del access token de Keycloak (realm donatrack).
 * Roles: realm_access.roles (ADMIN, DONANTE, BENEFICIARIA).
 * SERVICIO no es un rol de Keycloak: se otorga al token client_credentials
 * de un cliente de la lista configurada (su usuario es service-account-&lt;cliente&gt;).
 * Pertenencia: donante_id y entidad_ids, atributos que escribe CuentasKeycloak.
 */
public final class UsuarioToken {

    public static final String ADMIN = "ADMIN";
    public static final String DONANTE = "DONANTE";
    public static final String BENEFICIARIA = "BENEFICIARIA";
    public static final String SERVICIO = "SERVICIO";

    private static final Set<String> ROLES_KEYCLOAK = Set.of(ADMIN, DONANTE, BENEFICIARIA);

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

    public static Optional<Long> donanteId(Authentication auth) {
        if (!(auth instanceof JwtAuthenticationToken token)) {
            return Optional.empty();
        }
        return aLong(token.getToken().getClaims().get("donante_id"));
    }

    public static Set<Long> entidadIds(Authentication auth) {
        Set<Long> ids = new LinkedHashSet<>();
        if (auth instanceof JwtAuthenticationToken token) {
            Object claim = token.getToken().getClaims().get("entidad_ids");
            if (claim instanceof Collection<?> valores) {
                valores.forEach(v -> aLong(v).ifPresent(ids::add));
            } else {
                aLong(claim).ifPresent(ids::add);
            }
        }
        return ids;
    }

    /** Email del token solo si Keycloak lo da por verificado (registro con verificación o Google). */
    public static Optional<String> emailVerificado(Authentication auth) {
        if (!(auth instanceof JwtAuthenticationToken token)) {
            return Optional.empty();
        }
        Jwt jwt = token.getToken();
        String email = jwt.getClaimAsString("email");
        return Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified")) && email != null && !email.isBlank()
                ? Optional.of(email.trim().toLowerCase())
                : Optional.empty();
    }

    /**
     * Para operaciones donde el donante viene en el body y no en la URL.
     * Sin seguridad habilitada no hay token y no se restringe nada.
     */
    public static void exigirDonante(Long donanteId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken) || tieneRol(auth, ADMIN)) {
            return;
        }
        if (!tieneRol(auth, DONANTE) || donanteId == null || !donanteId.equals(donanteId(auth).orElse(null))) {
            throw new AccessDeniedException("La operación corresponde a otro donante");
        }
    }

    private static Optional<Long> aLong(Object valor) {
        if (valor == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(String.valueOf(valor).trim()));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
