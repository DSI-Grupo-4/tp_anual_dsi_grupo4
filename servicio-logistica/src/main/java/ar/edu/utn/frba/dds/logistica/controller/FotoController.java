package ar.edu.utn.frba.dds.logistica.controller;

import ar.edu.utn.frba.dds.logistica.dto.FotoRecuperada;
import ar.edu.utn.frba.dds.logistica.dto.FotoSubidaDTO;
import ar.edu.utn.frba.dds.logistica.service.FotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;

/** Subida y descarga de las fotos de las entregas (se guardan en MongoDB con GridFS). */
@RestController
@RequestMapping("/api/fotos")
public class FotoController {

    private final FotoService fotos;

    public FotoController(FotoService fotos) {
        this.fotos = fotos;
    }

    @Operation(
            summary = "Subir una foto de entrega",
            description = "Recibe una imagen JPEG, PNG o WebP de hasta 5 MB (parte multipart \"file\"), la guarda en MongoDB y devuelve su URL. "
                    + "Esa URL es la que se manda en la confirmación de recepción (POST /api/rutas/{id}/paradas/{id}/recepcion)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Foto guardada"),
            @ApiResponse(responseCode = "400", description = "El archivo está vacío o no es una imagen JPEG, PNG o WebP"),
            @ApiResponse(responseCode = "413", description = "La foto supera el tamaño máximo")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public FotoSubidaDTO subir(@RequestParam("file") MultipartFile file, Authentication auth) {
        // Se anota quién la sube: solo esa cuenta puede usarla después al confirmar una recepción.
        if (auth instanceof JwtAuthenticationToken) {
            return fotos.guardar(file, auth.getName());
        }
        return fotos.guardar(file);
    }

    @Operation(
            summary = "Obtener una foto",
            description = "Devuelve la imagen guardada. Las fotos no cambian: el navegador puede guardarlas en caché."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imagen"),
            @ApiResponse(responseCode = "404", description = "No existe la foto")
    })
    @GetMapping("/{id}")
    public ResponseEntity<InputStreamResource> obtener(@PathVariable String id) {
        FotoRecuperada foto = fotos.obtener(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(foto.contentType()))
                .contentLength(foto.longitud())
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                // La imagen se muestra, nunca se interpreta como otra cosa ni se ejecuta.
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .header("Content-Security-Policy", "default-src 'none'; sandbox")
                .body(new InputStreamResource(foto.contenido()));
    }
}
