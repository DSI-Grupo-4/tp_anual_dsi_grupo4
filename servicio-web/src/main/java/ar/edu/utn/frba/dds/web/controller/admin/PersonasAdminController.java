package ar.edu.utn.frba.dds.web.controller.admin;

import ar.edu.utn.frba.dds.web.admin.*;
import ar.edu.utn.frba.dds.web.client.donaciones.AdminDonacionesClient;
import ar.edu.utn.frba.dds.web.client.donaciones.AdminApi.*;
import ar.edu.utn.frba.dds.web.error.BackendRechazoException;
import ar.edu.utn.frba.dds.web.mensajes.Toasts;
import java.util.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Controller
@RequestMapping("/admin")
public class PersonasAdminController {
    private final AdminDonacionesClient api;
    public PersonasAdminController(AdminDonacionesClient api) { this.api=api; }
    @InitBinder("form") void limitar(WebDataBinder binder) { binder.setAutoGrowCollectionLimit(50); }
    public record Fila(Long id,String nombre,String tipo,Boolean activa,String email) { }
    private String email(List<Contacto> contactos) {
        return contactos==null?"Sin correo":contactos.stream().filter(c->"EMAIL".equals(c.tipo())).map(Contacto::valor).findFirst().orElse("Sin correo");
    }
    @GetMapping public String panel(Model m) { m.addAttribute("resumen",api.resumen()); return "admin/panel"; }
    @GetMapping("/{seccion:donantes|entidades}")
    public String lista(@PathVariable String seccion,@RequestParam Map<String,String> parametros,Model m) {
        boolean entidad="entidades".equals(seccion); var f=AdminVista.filtros(parametros,entidad?"activa":"activo");
        Pagina<Fila> p;
        if(entidad) { var datos=api.entidades(f); p=new Pagina<>(datos.content().stream().map(e->new Fila(e.id(),e.etiqueta(),e.personaJuridica().tipo(),e.activa(),email(e.personaJuridica().mediosContacto()))).toList(),datos.totalElements(),datos.totalPages(),datos.number()); }
        else { var datos=api.donantes(f); p=new Pagina<>(datos.content().stream().map(d->new Fila(d.id(),d.etiqueta(),d.tipo(),d.activo(),email(d.mediosContacto()))).toList(),datos.totalElements(),datos.totalPages(),datos.number()); }
        m.addAttribute("entidad",entidad); m.addAttribute("seccion",seccion); AdminVista.pagina(m,"/admin/"+seccion,f,p); return "admin/personas";
    }
    @GetMapping("/{seccion:donantes|entidades}/nuevo")
    public String nuevo(@PathVariable String seccion,@RequestParam(defaultValue="HUMANA") String tipo,Model m) {
        var form=new PersonaForm(); form.setTipo("entidades".equals(seccion)?"JURIDICA":tipo);
        if("JURIDICA".equals(form.getTipo())) form.getRepresentantes().add(new PersonaForm.RepresentanteForm());
        m.addAttribute("form",form); return preparar(m,seccion,null);
    }
    @GetMapping("/{seccion:donantes|entidades}/{id:[0-9]+}")
    public String detalle(@PathVariable String seccion,@PathVariable long id,@RequestParam(defaultValue="false") boolean editar,Model m) {
        PersonaForm form; boolean activa;
        if("entidades".equals(seccion)) { var e=api.entidad(id); form=PersonaForm.desde(e); activa=Boolean.TRUE.equals(e.activa()); m.addAttribute("necesidades",api.necesidades(id)); }
        else { var d=api.donante(id); form=PersonaForm.desde(d); activa=Boolean.TRUE.equals(d.activo()); }
        m.addAttribute("form",form); m.addAttribute("activa",activa);
        if(editar) { if(!activa) throw new ResponseStatusException(HttpStatus.CONFLICT,"Reactivá el registro antes de editarlo."); return preparar(m,seccion,id); }
        m.addAttribute("id",id); m.addAttribute("seccion",seccion); m.addAttribute("entidad","entidades".equals(seccion));
        return "admin/persona-detalle";
    }
    private String preparar(Model m,String seccion,Long id) {
        var form=(PersonaForm)m.getAttribute("form");
        if(form!=null && "JURIDICA".equals(form.getTipo()) && form.getRepresentantes().isEmpty())
            form.getRepresentantes().add(new PersonaForm.RepresentanteForm());
        m.addAttribute("seccion",seccion); m.addAttribute("entidad","entidades".equals(seccion)); m.addAttribute("id",id);
        return "admin/persona-form";
    }
    @PostMapping({"/{seccion:donantes|entidades}/nuevo","/{seccion:donantes|entidades}/{id:[0-9]+}"})
    public String guardar(@PathVariable String seccion,@PathVariable(required=false) Long id,
            @ModelAttribute("form") PersonaForm form,BindingResult errores,Model m,RedirectAttributes flash) {
        boolean entidad="entidades".equals(seccion);
        if(entidad) form.setTipo("JURIDICA");
        else if(id!=null) form.setTipo(api.donante(id).tipo()); // El tipo existente es inmutable.
        form.validar(errores,entidad);
        if(errores.hasErrors()) return preparar(m,seccion,id);
        try {
            Long resultado;
            if(entidad) resultado=(id==null?api.crearEntidad(form.datos(true,false)):api.editarEntidad(id,form.datos(true,true))).id();
            else resultado=(id==null?api.crearDonante(form.getTipo(),form.datos(false,false)):api.editarDonante(id,form.datos(false,true))).id();
            Toasts.exito(flash,id==null?"Registro creado correctamente.":"Cambios guardados.");
            return "redirect:/admin/"+seccion+"/"+resultado;
        } catch(BackendRechazoException e) { e.volcarEn(errores); return preparar(m,seccion,id); }
    }
    @PostMapping("/{seccion:donantes|entidades}/{id:[0-9]+}/{accion:baja|reactivar}")
    public String estado(@PathVariable String seccion,@PathVariable long id,@PathVariable String accion,RedirectAttributes flash) {
        try {
            if("entidades".equals(seccion)) { if("baja".equals(accion)) api.bajaEntidad(id); else api.reactivarEntidad(id); }
            else { if("baja".equals(accion)) api.bajaDonante(id); else api.reactivarDonante(id); }
            Toasts.exito(flash,"baja".equals(accion)?"Registro dado de baja. El historial se conserva.":"Registro reactivado.");
        } catch(BackendRechazoException e) { Toasts.error(flash,e.getMessage()); }
        return "redirect:/admin/"+seccion+"/"+id;
    }
    @GetMapping("/donantes/importar") public String importar() { return "admin/importar"; }
    @PostMapping("/donantes/importar")
    public String importar(@RequestParam MultipartFile archivo,Model m,RedirectAttributes flash) {
        if(archivo.isEmpty() || archivo.getOriginalFilename()==null || !archivo.getOriginalFilename().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            m.addAttribute("errorArchivo","Seleccioná un archivo CSV con contenido."); return "admin/importar";
        }
        try { var resultado=api.importar(archivo); Toasts.exito(flash,"Importación terminada: "+resultado.cantidadImportados()+" registros procesados."); return "redirect:/admin/donantes"; }
        catch(BackendRechazoException e) { m.addAttribute("errorArchivo",e.getMessage()); return "admin/importar"; }
    }
}
