package ar.edu.utn.frba.dds.logistica.service;

import ar.edu.utn.frba.dds.logistica.dto.FotoRecuperada;
import ar.edu.utn.frba.dds.logistica.dto.FotoSubidaDTO;
import ar.edu.utn.frba.dds.logistica.exception.ArchivoDemasiadoGrandeException;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

/**
 * Fotos de las entregas, guardadas en MongoDB con GridFS: viven en la misma base que el resto de los datos y las
 * comparten todas las instancias que usan esa base. Solo se aceptan imágenes JPEG, PNG y WebP, y se verifica el
 * contenido real del archivo (no alcanza con lo que diga el Content-Type, que lo elige quien sube el archivo).
 */
@Service
public class FotoService {

    private static final Set<String> TIPOS_ACEPTADOS = Set.of("image/jpeg", "image/png", "image/webp");
    private static final String RUTA_FOTOS = "/api/fotos/";
    private static final int LARGO_CABECERA = 12;
    private static final int LARGO_MAXIMO_NOMBRE = 80;

    private final GridFsTemplate gridFs;
    private final String urlBase;
    private final long maxBytes;

    public FotoService(GridFsTemplate gridFs,
                       @Value("${logistica.public-base-url:http://localhost:8083}") String urlBase,
                       @Value("${logistica.fotos.max-bytes:5242880}") long maxBytes) {
        this.gridFs = gridFs;
        this.urlBase = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
        this.maxBytes = maxBytes;
    }

    public FotoSubidaDTO guardar(MultipartFile archivo) {
        return guardar(archivo, null);
    }

    /** subidaPor: el sujeto (sub) del token de quien sube la foto; queda en los metadatos para validar la confirmación. */
    public FotoSubidaDTO guardar(MultipartFile archivo, String subidaPor) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }
        if (archivo.getSize() > maxBytes) {
            throw new ArchivoDemasiadoGrandeException(
                    "La foto supera el tamaño máximo de " + (maxBytes / (1024 * 1024)) + " MB");
        }
        String declarado = archivo.getContentType() == null ? "" : archivo.getContentType().toLowerCase(Locale.ROOT).trim();
        if (!TIPOS_ACEPTADOS.contains(declarado)) {
            throw new IllegalArgumentException("Solo se aceptan imágenes JPEG, PNG o WebP");
        }
        try {
            String real = tipoReal(cabecera(archivo));
            if (!declarado.equals(real)) {
                throw new IllegalArgumentException("El contenido del archivo no es una imagen JPEG, PNG o WebP válida");
            }
            String nombre = nombreSeguro(archivo.getOriginalFilename());
            ObjectId id = subidaPor == null
                    ? gridFs.store(archivo.getInputStream(), nombre, real)
                    : gridFs.store(archivo.getInputStream(), nombre, real, new Document("subidaPor", subidaPor));
            return new FotoSubidaDTO(id.toHexString(), urlDe(id.toHexString()));
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer el archivo subido", e);
        }
    }

    public FotoRecuperada obtener(String id) {
        if (id == null || !ObjectId.isValid(id)) {
            throw new NoSuchElementException("No existe la foto " + id);
        }
        GridFSFile archivo = gridFs.findOne(new Query(Criteria.where("_id").is(new ObjectId(id))));
        if (archivo == null) {
            throw new NoSuchElementException("No existe la foto " + id);
        }
        GridFsResource recurso = gridFs.getResource(archivo);
        try {
            return new FotoRecuperada(contentTypeDe(recurso), archivo.getLength(), recurso.getInputStream());
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer la foto " + id, e);
        }
    }

    /** La URL con la que se confirma una recepción para esa foto. */
    public String urlDe(String id) {
        return urlBase + RUTA_FOTOS + id;
    }

    /** El id de la foto si la URL es exactamente la que devolvió esta API; vacío para cualquier otra URL. */
    public Optional<String> idDeUrl(String url) {
        if (url == null) {
            return Optional.empty();
        }
        String limpia = url.trim();
        String prefijo = urlBase + RUTA_FOTOS;
        if (!limpia.startsWith(prefijo)) {
            return Optional.empty();
        }
        String id = limpia.substring(prefijo.length());
        return ObjectId.isValid(id) ? Optional.of(id) : Optional.empty();
    }

    /** Quién subió la foto (sub del token). Vacío si no existe o si se subió sin identificar a nadie. */
    public Optional<String> subidaPor(String id) {
        if (id == null || !ObjectId.isValid(id)) {
            return Optional.empty();
        }
        GridFSFile archivo = gridFs.findOne(new Query(Criteria.where("_id").is(new ObjectId(id))));
        if (archivo == null || archivo.getMetadata() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(archivo.getMetadata().getString("subidaPor"));
    }

    // ---- auxiliares ----

    private static byte[] cabecera(MultipartFile archivo) throws IOException {
        try (var entrada = archivo.getInputStream()) {
            return entrada.readNBytes(LARGO_CABECERA);
        }
    }

    /** El tipo según los primeros bytes del archivo, o null si no es ninguno de los aceptados. */
    static String tipoReal(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return "image/png";
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    static String nombreSeguro(String original) {
        if (original == null || original.isBlank()) {
            return "foto";
        }
        String limpio = original.replaceAll("[^A-Za-z0-9._-]", "_");
        return limpio.length() > LARGO_MAXIMO_NOMBRE ? limpio.substring(0, LARGO_MAXIMO_NOMBRE) : limpio;
    }

    private static String contentTypeDe(GridFsResource recurso) {
        try {
            return recurso.getContentType();
        } catch (RuntimeException e) {
            return "application/octet-stream";
        }
    }
}
