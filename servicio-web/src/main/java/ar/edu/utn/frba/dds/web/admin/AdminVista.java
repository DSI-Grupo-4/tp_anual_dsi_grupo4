package ar.edu.utn.frba.dds.web.admin;
import java.util.*;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.util.UriComponentsBuilder;
import ar.edu.utn.frba.dds.web.client.donaciones.AdminApi.Pagina;
@Component("adminVista")
public class AdminVista {
    public static final List<String> CATEGORIAS=List.of("ALIMENTOS","MOBILIARIO","VESTIMENTA");
    public static final List<String> SUBCATEGORIAS=List.of("SILLA","MESA","BANCO","COLCHON","FIDEOS_SECOS","ARROZ","LEGUMBRES_SECAS","ACEITE_VEGETAL","TOMATE","FRUTA","CAMPERA","REMERA","PANTALON","ROPA_INFANTIL","FRAZADA");
    public static final List<String> ESTADOS=List.of("EN_DEPOSITO","ASIGNACION_REALIZADA","LISTA_PARA_ENTREGAR","EN_TRASLADO","ENTREGADA","ENTREGA_FALLIDA","VENCIDA");
    public String texto(String valor) {
        if(valor==null || valor.isBlank()) return "Sin informar";
        String s=valor.toLowerCase(Locale.forLanguageTag("es")).replace('_',' ');
        return Character.toUpperCase(s.charAt(0))+s.substring(1);
    }
    public static Map<String,String> filtros(Map<String,String> entrada,String... extras) {
        var salida=new LinkedHashMap<String,String>();
        var claves=new ArrayList<>(List.of("q","page")); claves.addAll(List.of(extras));
        for(var k:claves) if(entrada.containsKey(k)) salida.put(k,entrada.get(k));
        salida.put("size","20"); return salida;
    }
    public static void pagina(Model m,String ruta,Map<String,String> filtros,Pagina<?> p) {
        m.addAttribute("pagina",p); m.addAttribute("filtros",filtros); m.addAttribute("rutaLista",ruta);
        m.addAttribute("anterior",enlace(ruta,filtros,Math.max(0,p.number()-1)));
        m.addAttribute("siguiente",enlace(ruta,filtros,p.number()+1));
    }
    private static String enlace(String ruta,Map<String,String> filtros,int page) {
        var uri=UriComponentsBuilder.fromPath(ruta);
        filtros.forEach((k,v)-> { if(!"page".equals(k)&&v!=null&&!v.isBlank()) uri.queryParam(k,v); });
        return uri.queryParam("page",page).build().encode().toUriString();
    }
}
