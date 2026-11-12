package ar.edu.utn.frba.dds.web.controller.beneficiaria;

import ar.edu.utn.frba.dds.web.beneficiaria.EntidadActual;
import ar.edu.utn.frba.dds.web.beneficiaria.PortalBeneficiariaService;
import ar.edu.utn.frba.dds.web.client.donaciones.DonacionApi;
import ar.edu.utn.frba.dds.web.client.donaciones.NecesidadesClient;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Panel de inicio de la entidad beneficiaria (/beneficiaria). */
@Controller
public class PanelBeneficiariaController {

    private static final int CANTIDAD_RECIENTES = 5;

    private final EntidadActual entidadActual;
    private final NecesidadesClient necesidades;
    private final PortalBeneficiariaService portal;

    public PanelBeneficiariaController(EntidadActual entidadActual, NecesidadesClient necesidades,
                                       PortalBeneficiariaService portal) {
        this.entidadActual = entidadActual;
        this.necesidades = necesidades;
        this.portal = portal;
    }

    @GetMapping("/beneficiaria")
    public String panel(Model model) {
        Long entidadId = entidadActual.id();
        List<DonacionApi> propias = portal.donacionesDe(entidadId);
        model.addAttribute("entidad", necesidades.entidad(entidadId));
        model.addAttribute("resumen", portal.resumenDonaciones(propias));
        model.addAttribute("recientes", propias.stream().limit(CANTIDAD_RECIENTES).toList());
        return "beneficiaria/panel";
    }
}
