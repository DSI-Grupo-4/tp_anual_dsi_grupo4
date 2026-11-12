package ar.edu.utn.frba.dds.logistica.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.utn.frba.dds.logistica.dto.FotoRecuperada;
import ar.edu.utn.frba.dds.logistica.dto.FotoSubidaDTO;
import ar.edu.utn.frba.dds.logistica.exception.ArchivoDemasiadoGrandeException;
import ar.edu.utn.frba.dds.logistica.exception.FotoExceptionHandler;
import ar.edu.utn.frba.dds.logistica.exception.GlobalExceptionHandler;
import ar.edu.utn.frba.dds.logistica.service.FotoService;
import java.io.ByteArrayInputStream;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class FotoControllerTest {

    private final FotoService servicio = mock(FotoService.class);
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(new FotoController(servicio))
                .setControllerAdvice(new GlobalExceptionHandler(), new FotoExceptionHandler())
                .build();
    }

    private static MockMultipartFile archivo() {
        return new MockMultipartFile("file", "f.png", "image/png", new byte[] {1, 2, 3});
    }

    @Test
    void subirDevuelve201ConElIdYLaUrl() throws Exception {
        when(servicio.guardar(any())).thenReturn(new FotoSubidaDTO("abc", "http://l/api/fotos/abc"));

        mvc.perform(multipart("/api/fotos").file(archivo()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("abc"))
                .andExpect(jsonPath("$.url").value("http://l/api/fotos/abc"));
    }

    @Test
    void unArchivoInvalidoEsUn400ConElMotivo() throws Exception {
        when(servicio.guardar(any())).thenThrow(new IllegalArgumentException("Solo se aceptan imágenes JPEG, PNG o WebP"));

        mvc.perform(multipart("/api/fotos").file(archivo()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("JPEG, PNG o WebP")));
    }

    @Test
    void unaFotoDemasiadoGrandeEsUn413() throws Exception {
        when(servicio.guardar(any())).thenThrow(new ArchivoDemasiadoGrandeException("La foto supera el tamaño máximo de 5 MB"));

        mvc.perform(multipart("/api/fotos").file(archivo()))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.error").value(containsString("5 MB")));
    }

    @Test
    void sinLaParteFileEsUn400() throws Exception {
        mvc.perform(multipart("/api/fotos").file(new MockMultipartFile("otra", "f.png", "image/png", new byte[] {1})))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("file")));
    }

    @Test
    void descargarSirveLaImagenSinQueSePuedaInterpretarComoOtraCosa() throws Exception {
        when(servicio.obtener("abc")).thenReturn(new FotoRecuperada("image/png", 3, new ByteArrayInputStream(new byte[] {1, 2, 3})));

        mvc.perform(get("/api/fotos/abc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(new byte[] {1, 2, 3}))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Content-Disposition", "inline"))
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'none'")))
                .andExpect(header().string("Cache-Control", containsString("immutable")));
    }

    @Test
    void unaFotoInexistenteEsUn404() throws Exception {
        when(servicio.obtener("zzz")).thenThrow(new NoSuchElementException("No existe la foto zzz"));

        mvc.perform(get("/api/fotos/zzz"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No existe la foto zzz"));
    }
}
