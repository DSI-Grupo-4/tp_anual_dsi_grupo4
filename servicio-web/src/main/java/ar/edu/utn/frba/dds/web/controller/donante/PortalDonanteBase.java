package ar.edu.utn.frba.dds.web.controller.donante;

import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Perfil;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteClient;
import ar.edu.utn.frba.dds.web.donante.DonanteActual;
import ar.edu.utn.frba.dds.web.donante.DonanteNoVinculadoException;
import ar.edu.utn.frba.dds.web.error.RecursoNoEncontradoException;
import ar.edu.utn.frba.dds.web.navegacion.ItemMenu;
import java.util.List;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Base de los controllers del portal del donante (Persona 4).
 *
 * Antes de cada request agrega al modelo el perfil del donante ("perfil") y, si es
 * persona jurídica, quita Incentivos del menú (requerimiento 7). Los @ModelAttribute de
 * un controller corren después de los de NavegacionModelAdvice, así que "menu" ya existe.
 */
public abstract class PortalDonanteBase {

    static final String SECCION_INCENTIVOS = "incentivos";

    protected final DonanteActual donanteActual;
    protected final PortalDonanteClient donaciones;

    protected PortalDonanteBase(DonanteActual donanteActual, PortalDonanteClient donaciones) {
        this.donanteActual = donanteActual;
        this.donaciones = donaciones;
    }

    @ModelAttribute("perfil")
    public Perfil perfilDelDonante(Model model) {
        long id = donanteActual.id();
        Perfil perfil;
        try {
            perfil = donaciones.perfil(id);
        } catch (RecursoNoEncontradoException e) {
            throw new DonanteNoVinculadoException(id, e);
        }
        if (perfil == null) {
            throw new DonanteNoVinculadoException(id, null);
        }
        if (!perfil.esHumana() && model.getAttribute("menu") instanceof List<?> menu) {
            model.addAttribute("menu", menu.stream()
                    .filter(item -> !(item instanceof ItemMenu i && SECCION_INCENTIVOS.equals(i.seccion())))
                    .toList());
        }
        return perfil;
    }
}
