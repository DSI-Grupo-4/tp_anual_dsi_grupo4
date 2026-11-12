package ar.edu.utn.frba.dds.web.error;

import ar.edu.utn.frba.dds.web.mensajes.Toast;
import ar.edu.utn.frba.dds.web.mensajes.Toasts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Convierte los errores de las APIs en páginas de error del cliente web.
 * Los rechazos de negocio (BackendRechazoException) conviene atraparlos en el
 * controller para mostrarlos en el formulario; si no se atrapan, llegan acá
 * y se muestran en una página con el mensaje del backend.
 */
@ControllerAdvice
public class WebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(WebExceptionHandler.class);
    static final String REINTENTO = "sesionRenovada";

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ModelAndView archivoGrande(org.springframework.web.multipart.MaxUploadSizeExceededException e) {
        return vista(HttpStatus.PAYLOAD_TOO_LARGE);
    }

    @ExceptionHandler(BackendNoDisponibleException.class)
    public ModelAndView noDisponible(BackendNoDisponibleException e) {
        log.warn("{} (status {})", e.getMessage(), e.getStatus(), e.getCause());
        ModelAndView vista = vista(HttpStatus.SERVICE_UNAVAILABLE);
        vista.addObject("servicio", e.getServicio().getNombreVisible());
        return vista;
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ModelAndView noEncontrado(RecursoNoEncontradoException e) {
        log.debug("404 de {}: {}", e.getServicio(), e.getMessage());
        return vista(HttpStatus.NOT_FOUND);
    }

    /**
     * La API rechazó el token o ya no se puede renovar: se cierra la sesión local y
     * se vuelve a pedir la página, que manda al login de Keycloak.
     * Si no había sesión de usuario, o el token recién obtenido también se rechaza
     * (parámetro {@value #REINTENTO}), es un problema de configuración y no de sesión:
     * se muestra 503 en lugar de redirigir en bucle.
     */
    @ExceptionHandler(SesionVencidaException.class)
    public Object sesionVencida(SesionVencidaException e, HttpServletRequest request) {
        if (!(SecurityContextHolder.getContext().getAuthentication() instanceof OAuth2AuthenticationToken)
                || request.getParameter(REINTENTO) != null) {
            log.warn("La API rechazó un token recién emitido ({}): revisar issuer y clientes de Keycloak. {}",
                    e.getServicio(), e.getMessage());
            ModelAndView vista = vista(HttpStatus.SERVICE_UNAVAILABLE);
            vista.addObject("servicio", e.getServicio().getNombreVisible());
            return vista;
        }
        log.info("Sesión vencida ({}): {}", e.getServicio(), e.getMessage());
        HttpSession sesion = request.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }
        SecurityContextHolder.clearContext();
        request.getSession(true);
        RequestContextUtils.getOutputFlashMap(request).put(Toasts.ATRIBUTO,
                new ArrayList<>(List.of(new Toast(Toast.Tipo.INFO, "Tu sesión venció. Volvé a ingresar para seguir."))));
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        return "redirect:" + ("GET".equals(request.getMethod()) ? ruta : "/") + "?" + REINTENTO;
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ModelAndView accesoDenegado(AccesoDenegadoException e) {
        log.info("403 de {}: {}", e.getServicio(), e.getMessage());
        return vista(HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BackendRechazoException.class)
    public ModelAndView rechazo(BackendRechazoException e) {
        ModelAndView vista = vista(HttpStatus.valueOf(e.getStatus()));
        vista.addObject("detalle", e.getMessage());
        return vista;
    }

    @ExceptionHandler(BackendException.class)
    public ModelAndView errorBackend(BackendException e) {
        log.error("Error inesperado de {} (status {}): {}", e.getServicio(), e.getStatus(), e.getMessage(), e);
        return vista(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static ModelAndView vista(HttpStatus status) {
        ModelAndView vista = new ModelAndView("error");
        vista.setStatus(status);
        vista.addObject("status", status.value());
        return vista;
    }
}
