package ar.edu.utn.frba.dds.web.client.logistica;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

/** Cliente de las fotos de Logística: sube el archivo que eligió la entidad y recibe la URL con la que se confirma la recepción. */
@Component
public class FotosClient {

    private final RestClient http;

    public FotosClient(@Qualifier("logistica") RestClient http) {
        this.http = http;
    }

    /**
     * POST /api/fotos (multipart, parte «file»). Logística verifica el contenido real del archivo y responde 400 si no es
     * una imagen JPEG, PNG o WebP, y 413 si supera el tamaño máximo.
     */
    public FotoSubidaApi subir(String nombreArchivo, String contentType, byte[] contenido) {
        HttpHeaders cabecerasDeLaParte = new HttpHeaders();
        cabecerasDeLaParte.setContentType(tipo(contentType));
        cabecerasDeLaParte.setContentDispositionFormData("file", nombreSeguro(nombreArchivo));

        MultiValueMap<String, Object> cuerpo = new LinkedMultiValueMap<>();
        cuerpo.add("file", new HttpEntity<>(new ByteArrayResource(contenido), cabecerasDeLaParte));

        return http.post().uri("/api/fotos")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(cuerpo)
                .retrieve()
                .body(FotoSubidaApi.class);
    }

    private static String nombreSeguro(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "foto";
        }
        return nombre.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private static MediaType tipo(String contentType) {
        try {
            return contentType == null || contentType.isBlank() ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType);
        } catch (RuntimeException e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}