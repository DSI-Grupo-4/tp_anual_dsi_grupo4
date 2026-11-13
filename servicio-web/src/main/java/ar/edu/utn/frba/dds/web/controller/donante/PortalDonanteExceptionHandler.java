package ar.edu.utn.frba.dds.web.controller.donante;

import ar.edu.utn.frba.dds.web.donante.DonanteNoVinculadoException;
import ar.edu.utn.frba.dds.web.navegacion.NavegacionService;
import ar.edu.utn.frba.dds.web.navegacion.Rol;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * Solo para los controllers del portal del donante: si no hay un Donante vinculado,
 * explica qué pasa en vez de mostrar el 404 genérico.
 * Va antes que WebExceptionHandler: Spring también busca handlers por la causa de la
 * excepción (acá, un RecursoNoEncontradoException) y, si no, ganaría el 404 genérico.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice(basePackageClasses = PortalDonanteExceptionHandler.class)
public class PortalDonanteExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(PortalDonanteExceptionHandler.class);

    private final NavegacionService navegacion;

    public PortalDonanteExceptionHandler(NavegacionService navegacion) {
        this.navegacion = navegacion;
    }

    @ExceptionHandler(DonanteNoVinculadoException.class)
    public ModelAndView noVinculado(DonanteNoVinculadoException e, HttpServletRequest request) {
        log.warn("Portal del donante sin donante vinculado (ID {})", e.getDonanteId());
        ModelAndView vista = new ModelAndView("donante/no-vinculado");
        vista.setStatus(HttpStatus.NOT_FOUND);
        // Los @ModelAttribute de NavegacionModelAdvice no corren para un @ExceptionHandler:
        // se agrega a mano lo que necesita el layout privado.
        String ruta = request.getRequestURI().substring(request.getContextPath().length());
        vista.addObject("rutaActual", ruta);
        vista.addObject("rolActual", Rol.DONANTE);
        vista.addObject("menu", navegacion.menu(Rol.DONANTE));
        vista.addObject("migas", navegacion.migas(Rol.DONANTE, ruta));
        return vista;
    }
}
