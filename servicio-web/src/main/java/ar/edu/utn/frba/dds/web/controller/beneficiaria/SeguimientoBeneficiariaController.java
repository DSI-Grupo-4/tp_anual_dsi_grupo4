package ar.edu.utn.frba.dds.web.controller.beneficiaria;

import ar.edu.utn.frba.dds.web.beneficiaria.EntidadActual;
import ar.edu.utn.frba.dds.web.logistica.EnvioEntidad;
import ar.edu.utn.frba.dds.web.logistica.MarcadoresSeguimiento;
import ar.edu.utn.frba.dds.web.logistica.SeguimientoService;
import ar.edu.utn.frba.dds.web.mapa.Marcador;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/** Seguimiento de las entregas que van hacia la entidad (/beneficiaria/seguimiento), con el mapa del camión. */
@Controller
public class SeguimientoBeneficiariaController {

    private final EntidadActual entidadActual;
    private final SeguimientoService seguimiento;
    private final MarcadoresSeguimiento marcadores;

    public SeguimientoBeneficiariaController(EntidadActual entidadActual, SeguimientoService seguimiento,
                                             MarcadoresSeguimiento marcadores) {
        this.entidadActual = entidadActual;
        this.seguimiento = seguimiento;
        this.marcadores = marcadores;
    }

    @GetMapping("/beneficiaria/seguimiento")
    public String seguimiento(Model model) {
        List<EnvioEntidad> envios = seguimiento.enviosDe(entidadActual.id());
        model.addAttribute("envios", envios);
        model.addAttribute("resumen", seguimiento.resumenDe(envios));
        model.addAttribute("marcadores", marcadores.paraEntidad(envios));
        return "beneficiaria/seguimiento";
    }

    /** Datos para refrescar el mapa: solo los camiones de las rutas en curso que le llevan algo a esta entidad. */
    @GetMapping(value = "/beneficiaria/seguimiento/ubicaciones.json", produces = "application/json")
    @ResponseBody
    public List<Marcador> ubicaciones() {
        return marcadores.paraEntidad(seguimiento.enviosDe(entidadActual.id()));
    }
}
