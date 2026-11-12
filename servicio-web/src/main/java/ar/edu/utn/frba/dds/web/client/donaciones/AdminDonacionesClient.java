package ar.edu.utn.frba.dds.web.client.donaciones;

import static ar.edu.utn.frba.dds.web.client.donaciones.AdminApi.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

/** API privada de administración; reutiliza propagación de credenciales y manejo de errores. */
@Component
public class AdminDonacionesClient {
    private final RestClient http;
    private final RestClient importador;
    public AdminDonacionesClient(@Qualifier("donaciones") RestClient http,
                                 @Qualifier("importacionDonantes") RestClient importador) {
        this.http=http; this.importador=importador;
    }
    private <T> T pagina(String recurso, Map<String,String> filtros, ParameterizedTypeReference<T> tipo) {
        return http.get().uri(b -> {
            b.path("/api/administracion/" + recurso);
            filtros.forEach((k,v) -> { if (v != null && !v.isBlank()) b.queryParam(k,"{"+k+"}"); });
            return b.build(filtros);
        }).retrieve().body(tipo);
    }
    public Pagina<Donante> donantes(Map<String,String> filtros) { return pagina("donantes",filtros,new ParameterizedTypeReference<>() {}); }
    public Pagina<Entidad> entidades(Map<String,String> filtros) { return pagina("entidades",filtros,new ParameterizedTypeReference<>() {}); }
    public Pagina<Donacion> donaciones(Map<String,String> filtros) { return pagina("donaciones",filtros,new ParameterizedTypeReference<>() {}); }
    public Resumen resumen() { return http.get().uri("/api/administracion/resumen").retrieve().body(Resumen.class); }
    public Donante donante(long id) { return http.get().uri("/api/donantes/{id}",id).retrieve().body(Donante.class); }
    public Entidad entidad(long id) { return http.get().uri("/api/entidades/{id}",id).retrieve().body(Entidad.class); }
    public Donacion donacion(long id) { return http.get().uri("/api/donaciones/{id}",id).retrieve().body(Donacion.class); }
    public List<Necesidad> necesidades(long id) { return http.get().uri("/api/entidades/{id}/necesidades",id).retrieve().body(new ParameterizedTypeReference<>() {}); }
    public List<Historial> historial(long id) { return http.get().uri("/api/donaciones/{id}/historial",id).retrieve().body(new ParameterizedTypeReference<>() {}); }
    public Donante crearDonante(String tipo, Object datos) {
        String ruta = "HUMANA".equals(tipo) ? "humanos" : "juridicos";
        return http.post().uri("/api/donantes/"+ruta).body(datos).retrieve().body(Donante.class);
    }
    public Donante editarDonante(long id,Object datos) { return http.put().uri("/api/donantes/{id}",id).body(datos).retrieve().body(Donante.class); }
    public Entidad crearEntidad(Object datos) { return http.post().uri("/api/entidades").body(datos).retrieve().body(Entidad.class); }
    public Entidad editarEntidad(long id,Object datos) { return http.put().uri("/api/entidades/{id}",id).body(datos).retrieve().body(Entidad.class); }
    public void bajaDonante(long id) { http.delete().uri("/api/donantes/{id}",id).retrieve().toBodilessEntity(); }
    public void reactivarDonante(long id) { http.patch().uri("/api/donantes/{id}/reactivar",id).retrieve().toBodilessEntity(); }
    public void bajaEntidad(long id) { http.delete().uri("/api/entidades/{id}",id).retrieve().toBodilessEntity(); }
    public void reactivarEntidad(long id) { http.patch().uri("/api/entidades/{id}/reactivar",id).retrieve().toBodilessEntity(); }
    public Importacion importar(MultipartFile archivo) {
        var partes=new LinkedMultiValueMap<String,Object>(); partes.add("archivo",archivo.getResource());
        return importador.post().uri("/api/donantes/importar").contentType(MediaType.MULTIPART_FORM_DATA)
                .body(partes).retrieve().body(Importacion.class);
    }
    public List<Donacion> crearDonacion(Object datos) { return http.post().uri("/api/donaciones").body(datos).retrieve().body(new ParameterizedTypeReference<>() {}); }
    public void editarDonacion(long id,Object datos) { http.put().uri("/api/donaciones/{id}",id).body(datos).retrieve().toBodilessEntity(); }
    public void eliminarDonacion(long id) { http.delete().uri("/api/donaciones/{id}",id).retrieve().toBodilessEntity(); }
    public void vencer(long id,String motivo) { http.patch().uri("/api/donaciones/{id}/estado",id)
            .body(Map.of("nuevoEstado","VENCIDA","justificacion",motivo)).retrieve().toBodilessEntity(); }
    public Resultado ejecutar(long id) { return http.get().uri("/api/donaciones/{id}/candidatas",id).retrieve().body(Resultado.class); }
    public Resultado ultimoResultado(long id) { return http.get().uri("/api/donaciones/{id}/candidatas/ultimo",id).retrieve().body(Resultado.class); }
    public void asignar(long id,long entidad) { http.post().uri("/api/donaciones/{id}/asignar",id)
            .body(Map.of("entidadId",entidad)).retrieve().toBodilessEntity(); }
}
