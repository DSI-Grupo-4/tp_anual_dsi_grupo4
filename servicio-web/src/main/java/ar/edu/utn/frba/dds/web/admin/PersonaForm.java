package ar.edu.utn.frba.dds.web.admin;
import ar.edu.utn.frba.dds.web.client.donaciones.AdminApi.*;
import java.util.*;
import lombok.Data;
import org.springframework.validation.Errors;
/** Modelo de formulario, independiente del dominio del backend. */
@Data
public class PersonaForm {
    private String tipo="HUMANA", nombre="", apellido="", documento="", genero="", razonSocial="", tipoOrganizacion="ONG", rubro="", descripcion="";
    private Integer edad;
    private String calle="", numero="", ciudad="", provincia="";
    private List<ContactoForm> contactos=new ArrayList<>(List.of(new ContactoForm()));
    private int preferido;
    private List<RepresentanteForm> representantes=new ArrayList<>();
    @Data public static class ContactoForm {
        private String tipo="EMAIL", valor="";
        public ContactoForm() { }
        public ContactoForm(String tipo,String valor) { this.tipo=tipo; this.valor=valor; }
    }
    @Data public static class RepresentanteForm {
        private String nombre="", apellido="", documento="", email="";
        public Humana datos() { return new Humana(nombre,apellido,null,documento,null,null,List.of(new Contacto("EMAIL",email,true))); }
    }
    public void validar(Errors e, boolean entidad) {
        if (!Set.of("HUMANA","JURIDICA").contains(tipo)) e.rejectValue("tipo","dato","Elegí un tipo de persona válido.");
        if (entidad || "JURIDICA".equals(tipo)) {
            obligatorio(e,"razonSocial",razonSocial);
            if (!Set.of("ONG","EMPRESA","INSTITUCION","GUBERNAMENTAL").contains(tipoOrganizacion)) e.rejectValue("tipoOrganizacion","dato","Elegí un tipo de organización.");
            if (representantes.isEmpty()) e.rejectValue("representantes","dato","Agregá al menos una persona representante.");
            if (!entidad && representantes.size()>1) e.rejectValue("representantes","dato","Una organización donante admite un único representante.");
        } else {
            obligatorio(e,"nombre",nombre); obligatorio(e,"apellido",apellido); obligatorio(e,"documento",documento);
            if (edad == null || edad < 0 || edad > 130) e.rejectValue("edad","dato","Ingresá una edad entre 0 y 130.");
            if (!Set.of("MASCULINO","FEMENINO","X").contains(genero)) e.rejectValue("genero","dato","Elegí una opción.");
        }
        if (entidad || "HUMANA".equals(tipo)) {
            obligatorio(e,"calle",calle); obligatorio(e,"numero",numero); obligatorio(e,"ciudad",ciudad); obligatorio(e,"provincia",provincia);
        }
        if (contactos.isEmpty() || contactos.size()>20) e.rejectValue("contactos","dato","Agregá entre uno y veinte contactos.");
        boolean email=false;
        for (int i=0;i<contactos.size();i++) {
            var c=contactos.get(i);
            if(c==null) { e.reject("dato","Revisá los contactos."); continue; }
            obligatorio(e,"contactos["+i+"].valor",c.valor);
            if (!Set.of("EMAIL","TELEFONO","WHATSAPP").contains(c.tipo)) e.rejectValue("contactos["+i+"].tipo","dato","Tipo de contacto inválido.");
            if ("EMAIL".equals(c.tipo)) { email=true; if(!emailValido(c.valor)) e.rejectValue("contactos["+i+"].valor","dato","Ingresá un correo válido, por ejemplo ana@correo.com."); }
        }
        if (!email) e.rejectValue("contactos","dato","El correo electrónico es obligatorio.");
        if (entidad && contactos.stream().noneMatch(c->c!=null && "TELEFONO".equals(c.tipo) && c.valor!=null && !c.valor.isBlank()))
            e.rejectValue("contactos","dato","Incluí un teléfono de la entidad.");
        if (entidad) obligatorio(e,"descripcion",descripcion);
        if (preferido<0 || preferido>=contactos.size()) e.rejectValue("preferido","dato","Elegí un contacto preferido.");
        for (int i=0;i<representantes.size();i++) {
            var r=representantes.get(i); String p="representantes["+i+"].";
            obligatorio(e,p+"nombre",r.nombre); obligatorio(e,p+"apellido",r.apellido); obligatorio(e,p+"documento",r.documento);
            if (!emailValido(r.email)) e.rejectValue(p+"email","dato","Ingresá el correo de la persona representante.");
        }
    }
    private static boolean emailValido(String s) { return s != null && s.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"); }
    private static void obligatorio(Errors e,String campo,String v) { if(v==null || v.isBlank()) e.rejectValue(campo,"dato","Completá este campo."); }
    public Direccion direccion() { return new Direccion(calle,numero,new Ciudad(ciudad,new Provincia(provincia))); }
    public List<Contacto> contactosApi() {
        var salida=new ArrayList<Contacto>();
        for(int i=0;i<contactos.size();i++) salida.add(new Contacto(contactos.get(i).tipo,contactos.get(i).valor.trim(),i==preferido));
        return salida;
    }
    public Object datos(boolean entidad, boolean edicion) {
        var reps=representantes.stream().map(RepresentanteForm::datos).toList();
        var juridica=new Juridica(razonSocial,tipoOrganizacion,rubro,contactosApi(),reps);
        if(entidad) return new Entidad(null,null,descripcion,juridica,direccion());
        if(edicion) return new Donante(null,null,tipo,nombre,apellido,edad,documento,genero.isBlank()?null:genero,
                razonSocial,tipoOrganizacion,rubro,contactosApi(),direccion(),reps);
        return "HUMANA".equals(tipo) ? new Humana(nombre,apellido,edad,documento,genero,direccion(),contactosApi()) : juridica;
    }
    public static PersonaForm desde(Donante d) {
        var f=new PersonaForm(); f.tipo=d.tipo(); f.nombre=n(d.nombre()); f.apellido=n(d.apellido()); f.edad=d.edad();
        f.documento=n(d.documento()); f.genero=n(d.genero()); f.razonSocial=n(d.razonSocial());
        f.tipoOrganizacion=d.tipoOrganizacion()==null?"ONG":d.tipoOrganizacion(); f.rubro=n(d.rubro());
        f.cargar(d.direccion(),d.mediosContacto(),d.representantes()); return f;
    }
    public static PersonaForm desde(Entidad e) {
        var f=new PersonaForm(); f.tipo="JURIDICA"; f.razonSocial=e.personaJuridica().razonSocial();
        f.tipoOrganizacion=e.personaJuridica().tipo(); f.rubro=n(e.personaJuridica().rubro()); f.descripcion=n(e.descripcion());
        f.cargar(e.direccion(),e.personaJuridica().mediosContacto(),e.personaJuridica().representantes()); return f;
    }
    private void cargar(Direccion d,List<Contacto> cs,List<Humana> reps) {
        if(d!=null) { calle=n(d.calle()); numero=n(d.numero()); if(d.ciudad()!=null) { ciudad=n(d.ciudad().nombre()); if(d.ciudad().provincia()!=null) provincia=n(d.ciudad().provincia().nombre()); } }
        if(cs!=null && !cs.isEmpty()) { contactos=new ArrayList<>(); for(var c:cs) { if(Boolean.TRUE.equals(c.esPreferido())) preferido=contactos.size(); contactos.add(new ContactoForm(c.tipo(),c.valor())); } }
        if(reps!=null) for(var r:reps) {
            var f=new RepresentanteForm(); f.nombre=n(r.nombre()); f.apellido=n(r.apellido()); f.documento=n(r.documento());
            if(r.mediosContacto()!=null) r.mediosContacto().stream().filter(c->"EMAIL".equals(c.tipo())).findFirst().ifPresent(c->f.email=c.valor());
            representantes.add(f);
        }
    }
    private static String n(String s) { return s==null?"":s; }
}
