package ar.edu.utn.frba.dds.web.controller.beneficiaria;

import ar.edu.utn.frba.dds.web.beneficiaria.EntidadActual;
import ar.edu.utn.frba.dds.web.beneficiaria.FotosDeDonacion;
import ar.edu.utn.frba.dds.web.beneficiaria.PortalBeneficiariaService;
import ar.edu.utn.frba.dds.web.client.donaciones.DonacionApi;
import ar.edu.utn.frba.dds.web.client.donaciones.DonacionesClient;
import ar.edu.utn.frba.dds.web.error.RecursoNoEncontradoException;
import ar.edu.utn.frba.dds.web.error.ServicioBackend;
import ar.edu.utn.frba.dds.web.navegacion.Miga;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Donaciones asignadas a la entidad y su estado (/beneficiaria/donaciones). */
@Controller
@RequestMapping("/beneficiaria/donaciones")
public class DonacionesAsignadasController {

    public static final String PENDIENTES = "pendientes";
    public static final String RECIBIDAS = "recibidas";
    public static final String TODAS = "todas";

    private final EntidadActual entidadActual;
    private final DonacionesClient donaciones;
    private final PortalBeneficiariaService portal;
    private final FotosDeDonacion fotosDeDonacion;

    public DonacionesAsignadasController(EntidadActual entidadActual, DonacionesClient donaciones,
                                         PortalBeneficiariaService portal, FotosDeDonacion fotosDeDonacion) {
        this.entidadActual = entidadActual;
        this.donaciones = donaciones;
        this.portal = portal;
        this.fotosDeDonacion = fotosDeDonacion;
    }

    @GetMapping
    public String lista(@RequestParam(defaultValue = TODAS) String filtro, Model model) {
        List<DonacionApi> propias = portal.donacionesDe(entidadActual.id());
        String filtroValido = PENDIENTES.equals(filtro) || RECIBIDAS.equals(filtro) ? filtro : TODAS;
        List<DonacionApi> visibles = switch (filtroValido) {
            case PENDIENTES -> portal.pendientes(propias);
            case RECIBIDAS -> portal.recibidas(propias);
            default -> propias;
        };
        model.addAttribute("donaciones", visibles);
        model.addAttribute("filtro", filtroValido);
        model.addAttribute("resumen", portal.resumenDonaciones(propias));
        return "beneficiaria/donaciones";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        DonacionApi donacion = donaciones.donacion(id);
        // Una entidad solo ve sus donaciones: si es de otra, se responde como si no existiera.
        if (donacion == null || !entidadActual.id().equals(donacion.entidadBeneficiariaId())) {
            throw new RecursoNoEncontradoException(ServicioBackend.DONACIONES, "No existe la donación " + id);
        }
        model.addAttribute("donacion", donacion);
        model.addAttribute("historial", donaciones.historial(id));
        model.addAttribute("fotos", "ENTREGADA".equals(donacion.estadoActual()) ? fotosDeDonacion.de(id) : List.of());
        model.addAttribute("migas", List.of(
                Miga.clave("nav.beneficiaria.panel", "/beneficiaria"),
                Miga.clave("nav.beneficiaria.donaciones", "/beneficiaria/donaciones"),
                Miga.texto(donacion.descripcionItem() == null ? "#" + id : donacion.descripcionItem(), null)));
        return "beneficiaria/donacion-detalle";
    }
}
