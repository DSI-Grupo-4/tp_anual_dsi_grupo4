package ar.edu.utn.frba.dds.web.controller.beneficiaria;

import ar.edu.utn.frba.dds.web.beneficiaria.EntidadActual;
import ar.edu.utn.frba.dds.web.logistica.EnvioEntidad;
import ar.edu.utn.frba.dds.web.logistica.SeguimientoService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Seguimiento de las entregas que van hacia la entidad (/beneficiaria/seguimiento). */
@Controller
public class SeguimientoBeneficiariaController {

    private final EntidadActual entidadActual;
    private final SeguimientoService seguimiento;

    public SeguimientoBeneficiariaController(EntidadActual entidadActual, SeguimientoService seguimiento) {
        this.entidadActual = entidadActual;
        this.seguimiento = seguimiento;
    }

    @GetMapping("/beneficiaria/seguimiento")
    public String seguimiento(Model model) {
        List<EnvioEntidad> envios = seguimiento.enviosDe(entidadActual.id());
        model.addAttribute("envios", envios);
        model.addAttribute("resumen", seguimiento.resumenDe(envios));
        return "beneficiaria/seguimiento";
    }
}
