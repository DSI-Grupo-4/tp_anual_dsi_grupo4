package ar.edu.utn.frba.dds.web.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * Convierte los errores de las APIs en páginas de error del cliente web.
 * Los rechazos de negocio (BackendRechazoException) conviene atraparlos en el
 * controller para mostrarlos en el formulario; si no se atrapan, llegan acá
 * y se muestran en una página con el mensaje del backend.
 */
@ControllerAdvice
public class WebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(WebExceptionHandler.class);

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
