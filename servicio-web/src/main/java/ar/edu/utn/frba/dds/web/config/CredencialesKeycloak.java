package ar.edu.utn.frba.dds.web.config;

import ar.edu.utn.frba.dds.web.client.PropagadorCredenciales;
import ar.edu.utn.frba.dds.web.error.BackendNoDisponibleException;
import ar.edu.utn.frba.dds.web.error.ServicioBackend;
import ar.edu.utn.frba.dds.web.error.SesionVencidaException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Credenciales de las llamadas a las APIs (D-045):
 * <ul>
 *   <li>con un usuario logueado, su access token (se renueva solo con el refresh token);</li>
 *   <li>sin usuario, o dentro de {@link #comoServicio}, el token client_credentials de servicio-web
 *       (lo usa la landing pública, que muestra datos agregados sin sesión).</li>
 * </ul>
 * Con SEGURIDAD_HABILITADA=false no agrega nada, igual que antes.
 */
@Component
public class CredencialesKeycloak implements PropagadorCredenciales {

    private static final ThreadLocal<Boolean> COMO_SERVICIO = ThreadLocal.withInitial(() -> false);

    private final boolean habilitada;
    private final OAuth2AuthorizedClientManager usuarios;
    private final OAuth2AuthorizedClientManager servicio;

    public CredencialesKeycloak(@Value("${donatrack.web.auth.propagar-token:false}") boolean habilitada,
                                OAuth2AuthorizedClientManager usuarios,
                                ClientRegistrationRepository registros,
                                OAuth2AuthorizedClientService clientesServicio) {
        this.habilitada = habilitada;
        this.usuarios = usuarios;
        this.servicio = SeguridadConfig.tokenServicio(registros, clientesServicio);
    }

    /**
     * Ejecuta la llamada con el token de servicio aunque haya un usuario logueado.
     * Un 401 acá no es la sesión del usuario: es el cliente de servicio mal configurado.
     */
    public static <T> T comoServicio(Supplier<T> llamada) {
        boolean anterior = COMO_SERVICIO.get();
        COMO_SERVICIO.set(true);
        try {
            return llamada.get();
        } catch (SesionVencidaException e) {
            throw new BackendNoDisponibleException(e.getServicio(), 401, e);
        } finally {
            COMO_SERVICIO.set(anterior);
        }
    }

    @Override
    public void aplicar(HttpHeaders headers) {
        if (!habilitada) {
            return;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        OAuth2AuthorizedClient cliente = !COMO_SERVICIO.get() && auth instanceof OAuth2AuthenticationToken usuario
                ? deUsuario(usuario)
                : deServicio();
        headers.setBearerAuth(cliente.getAccessToken().getTokenValue());
    }

    private OAuth2AuthorizedClient deUsuario(OAuth2AuthenticationToken usuario) {
        OAuth2AuthorizeRequest.Builder pedido = OAuth2AuthorizeRequest
                .withClientRegistrationId(RolesKeycloak.REGISTRATION_ID)
                .principal(usuario);
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes atributos) {
            pedido.attribute(HttpServletRequest.class.getName(), atributos.getRequest());
            if (atributos.getResponse() != null) {
                pedido.attribute(HttpServletResponse.class.getName(), atributos.getResponse());
            }
        }
        OAuth2AuthorizedClient cliente;
        try {
            cliente = usuarios.authorize(pedido.build());
        } catch (OAuth2AuthorizationException e) {
            throw new SesionVencidaException(ServicioBackend.AUTENTICACION, "No se pudo renovar la sesión", e);
        } catch (RuntimeException e) {
            throw new BackendNoDisponibleException(ServicioBackend.AUTENTICACION, 0, e);
        }
        if (cliente == null) {
            throw new SesionVencidaException(ServicioBackend.AUTENTICACION, "La sesión no tiene un token vigente", null);
        }
        return cliente;
    }

    /** Si Keycloak no responde, falla como un backend caído: la landing muestra esa parte sin datos. */
    private OAuth2AuthorizedClient deServicio() {
        OAuth2AuthorizedClient cliente;
        try {
            cliente = servicio.authorize(OAuth2AuthorizeRequest
                    .withClientRegistrationId(SeguridadConfig.REGISTRATION_SERVICIO)
                    .principal("servicio-web")
                    .build());
        } catch (RuntimeException e) {
            throw new BackendNoDisponibleException(ServicioBackend.AUTENTICACION, 0, e);
        }
        if (cliente == null) {
            throw new BackendNoDisponibleException(ServicioBackend.AUTENTICACION, 0, null);
        }
        return cliente;
    }
}
