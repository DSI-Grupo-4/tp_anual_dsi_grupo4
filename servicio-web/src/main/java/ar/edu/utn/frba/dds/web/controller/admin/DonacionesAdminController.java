package ar.edu.utn.frba.dds.web.controller.admin;
import ar.edu.utn.frba.dds.web.admin.*;
import ar.edu.utn.frba.dds.web.client.donaciones.*;
import ar.edu.utn.frba.dds.web.error.BackendRechazoException;
import ar.edu.utn.frba.dds.web.mensajes.Toasts;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
@Controller
@RequestMapping("/admin")
public class DonacionesAdminController {
    private final AdminDonacionesClient api;
    public DonacionesAdminController(AdminDonacionesClient api) { this.api=api; }
    @InitBinder("form") void limitar(WebDataBinder binder) { binder.setAutoGrowCollectionLimit(50); }
    @ModelAttribute void catalogos(Model m) {
        m.addAttribute("categorias",AdminVista.CATEGORIAS); m.addAttribute("subcategorias",AdminVista.SUBCATEGORIAS);
        m.addAttribute("estados",AdminVista.ESTADOS); m.addAttribute("unidades",List.of("UNIDAD","KILOGRAMO","CAJA","PAQUETE","LITRO"));
    }
    @GetMapping({"/donaciones","/asignaciones"})
    public String lista(@RequestParam Map<String,String> parametros,jakarta.servlet.http.HttpServletRequest request,Model m) {
        boolean asignar=request.getRequestURI().endsWith("asignaciones");
        var f=AdminVista.filtros(parametros,"estado","categoria","subcategoria","donanteId","entidadId","desde","hasta");
        if(asignar) f.put("estado","EN_DEPOSITO");
        m.addAttribute("asignar",asignar); AdminVista.pagina(m,asignar?"/admin/asignaciones":"/admin/donaciones",f,api.donaciones(f));
        return "admin/donaciones";
    }
    @GetMapping("/donaciones/nueva")
    public String nueva(@RequestParam(required=false) Long donanteId,@RequestParam(defaultValue="") String q,Model m) {
        var f=new DonacionForm(); f.setDonanteId(donanteId); m.addAttribute("form",f); m.addAttribute("q",q); return preparar(m,null,q);
    }
    private String preparar(Model m,Long id,String q) {
        m.addAttribute("id",id);
        if(id==null) {
            m.addAttribute("donantes",api.donantes(Map.of("q",q,"activo","true","page","0","size","20")));
            var f=(DonacionForm)m.getAttribute("form");
            if(f!=null && f.getDonanteId()!=null) m.addAttribute("donanteElegido",api.donante(f.getDonanteId()));
        }
        return "admin/donacion-form";
    }
    @GetMapping("/donaciones/{id:[0-9]+}")
    public String detalle(@PathVariable long id,@RequestParam(defaultValue="false") boolean editar,Model m) {
        var d=api.donacion(id);
        if(editar) { if(!d.editable()) throw new ResponseStatusException(HttpStatus.CONFLICT,"Solo se editan donaciones en depósito."); m.addAttribute("form",DonacionForm.desde(d)); return preparar(m,id,""); }
        m.addAttribute("donacion",d); m.addAttribute("historial",api.historial(id)); return "admin/donacion-detalle";
    }
    @PostMapping({"/donaciones/nueva","/donaciones/{id:[0-9]+}"})
    public String guardar(@PathVariable(required=false) Long id,@Valid @ModelAttribute("form") DonacionForm f,
            BindingResult errores,Model m,RedirectAttributes flash) {
        if(id==null) {
            if(f.getDonanteId()==null) errores.rejectValue("donanteId","dato","Elegí la persona donante.");
            if(f.getDescripcion()==null || f.getDescripcion().isBlank()) errores.rejectValue("descripcion","dato","Describí la carga.");
        }
        if(errores.hasErrors()) return preparar(m,id,"");
        try {
            if(id!=null) { api.editarDonacion(id,f.datos(true)); Toasts.exito(flash,"Donación actualizada."); return "redirect:/admin/donaciones/"+id; }
            var creadas=api.crearDonacion(f.datos(false)); flash.addFlashAttribute("creadas",creadas);
            Toasts.exito(flash,"Carga registrada: "+creadas.size()+" donaciones generadas por segmentación.");
            return "redirect:/admin/donaciones";
        } catch(BackendRechazoException e) { e.volcarEn(errores); return preparar(m,id,""); }
    }
    @PostMapping("/donaciones/{id:[0-9]+}/vencer")
    public String vencer(@PathVariable long id,@RequestParam String justificacion,RedirectAttributes flash) {
        if(justificacion.isBlank()) Toasts.error(flash,"Indicá el motivo para marcar la donación como vencida.");
        else try { api.vencer(id,justificacion); Toasts.exito(flash,"Donación marcada como vencida."); } catch(BackendRechazoException e) { Toasts.error(flash,e.getMessage()); }
        return "redirect:/admin/donaciones/"+id;
    }
    @PostMapping("/donaciones/{id:[0-9]+}/eliminar")
    public String eliminar(@PathVariable long id,RedirectAttributes flash) {
        try { api.eliminarDonacion(id); Toasts.exito(flash,"Donación eliminada. La carga original se conserva."); return "redirect:/admin/donaciones"; }
        catch(BackendRechazoException e) { Toasts.error(flash,e.getMessage()); return "redirect:/admin/donaciones/"+id; }
    }
    @GetMapping("/asignaciones/{id:[0-9]+}")
    public String asignacion(@PathVariable long id,Model m) {
        m.addAttribute("donacion",api.donacion(id));
        if(!m.containsAttribute("resultado")) m.addAttribute("resultado",api.ultimoResultado(id));
        return "admin/asignacion";
    }
    @PostMapping("/asignaciones/{id:[0-9]+}/ejecutar")
    public String ejecutar(@PathVariable long id,RedirectAttributes flash) {
        try { flash.addFlashAttribute("resultado",api.ejecutar(id)); Toasts.exito(flash,"Algoritmos ejecutados. Compará los resultados antes de confirmar."); }
        catch(BackendRechazoException e) { Toasts.error(flash,e.getMessage()); }
        return "redirect:/admin/asignaciones/"+id;
    }
    @PostMapping("/asignaciones/{id:[0-9]+}/confirmar")
    public String confirmar(@PathVariable long id,@RequestParam long entidadId,RedirectAttributes flash) {
        try { api.asignar(id,entidadId); Toasts.exito(flash,"Asignación confirmada. La donación quedó pendiente de planificación."); return "redirect:/admin/donaciones/"+id; }
        catch(BackendRechazoException e) { Toasts.error(flash,e.getMessage()); return "redirect:/admin/asignaciones/"+id; }
    }
}
