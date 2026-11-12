package ar.edu.utn.frba.dds.logistica.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.logistica.dto.FotoRecuperada;
import ar.edu.utn.frba.dds.logistica.dto.FotoSubidaDTO;
import ar.edu.utn.frba.dds.logistica.exception.ArchivoDemasiadoGrandeException;
import com.mongodb.client.gridfs.model.GridFSFile;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.NoSuchElementException;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.mock.web.MockMultipartFile;

class FotoServiceTest {

    private static final ObjectId ID = new ObjectId("6730a1f2c3d4e5f607182930");

    // Los primeros bytes reales de cada formato
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 16, 'J', 'F', 'I', 'F'};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 13};
    private static final byte[] WEBP = {'R', 'I', 'F', 'F', 1, 2, 3, 4, 'W', 'E', 'B', 'P'};

    private final GridFsTemplate gridFs = mock(GridFsTemplate.class);
    private FotoService servicio;

    @BeforeEach
    void preparar() {
        servicio = new FotoService(gridFs, "http://logistica.ejemplo.org/", 1024);
        when(gridFs.store(any(InputStream.class), anyString(), anyString())).thenReturn(ID);
    }

    private static MockMultipartFile archivo(String tipo, byte[] contenido) {
        return new MockMultipartFile("file", "mi foto (1).jpg", tipo, contenido);
    }

    @Test
    void guardaCadaFormatoAceptadoYDevuelveLaUrlPublica() {
        FotoSubidaDTO jpeg = servicio.guardar(archivo("image/jpeg", JPEG));
        servicio.guardar(archivo("image/png", PNG));
        servicio.guardar(archivo("image/webp", WEBP));

        assertThat(jpeg.getId()).isEqualTo("6730a1f2c3d4e5f607182930");
        assertThat(jpeg.getUrl()).isEqualTo("http://logistica.ejemplo.org/api/fotos/6730a1f2c3d4e5f607182930");
        verify(gridFs).store(any(InputStream.class), eq("mi_foto__1_.jpg"), eq("image/jpeg"));
        verify(gridFs).store(any(InputStream.class), anyString(), eq("image/png"));
        verify(gridFs).store(any(InputStream.class), anyString(), eq("image/webp"));
    }

    @Test
    void unArchivoVacioSeRechaza() {
        assertThatThrownBy(() -> servicio.guardar(archivo("image/jpeg", new byte[0])))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("vacío");
    }

    @Test
    void unaFotoMasGrandeQueElMaximoSeRechazaComoDemasiadoGrande() {
        assertThatThrownBy(() -> servicio.guardar(archivo("image/jpeg", new byte[1025])))
                .isInstanceOf(ArchivoDemasiadoGrandeException.class);
        verify(gridFs, never()).store(any(InputStream.class), anyString(), anyString());
    }

    @Test
    void soloSeAceptanImagenesJpegPngOWebp() {
        assertThatThrownBy(() -> servicio.guardar(archivo("image/gif", JPEG)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("JPEG, PNG o WebP");
        assertThatThrownBy(() -> servicio.guardar(archivo("image/svg+xml", JPEG)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> servicio.guardar(archivo("application/pdf", JPEG)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> servicio.guardar(archivo(null, JPEG)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void unArchivoDisfrazadoDeImagenSeRechazaPorSuContenido() {
        byte[] html = "<html><script>alert(1)</script></html>".getBytes();

        assertThatThrownBy(() -> servicio.guardar(archivo("image/jpeg", html)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("no es una imagen");
        // un PNG que dice ser JPEG tampoco pasa: el tipo declarado tiene que coincidir con el real
        assertThatThrownBy(() -> servicio.guardar(archivo("image/jpeg", PNG)))
                .isInstanceOf(IllegalArgumentException.class);
        verify(gridFs, never()).store(any(InputStream.class), anyString(), anyString());
    }

    @Test
    void elNombreDelArchivoSeLimpiaYTieneLargoMaximo() {
        assertThat(FotoService.nombreSeguro("../../etc/pas swd.png")).isEqualTo(".._.._etc_pas_swd.png");
        assertThat(FotoService.nombreSeguro(null)).isEqualTo("foto");
        assertThat(FotoService.nombreSeguro("   ")).isEqualTo("foto");
        assertThat(FotoService.nombreSeguro("a".repeat(200))).hasSize(80);
    }

    @Test
    void unIdQueNoEsDeMongoEsUn404SinConsultarLaBase() {
        assertThatThrownBy(() -> servicio.obtener("no-es-un-id"))
                .isInstanceOf(NoSuchElementException.class);
        verify(gridFs, never()).findOne(any(Query.class));
    }

    @Test
    void unaFotoInexistenteEsUn404() {
        when(gridFs.findOne(any(Query.class))).thenReturn(null);

        assertThatThrownBy(() -> servicio.obtener("6730a1f2c3d4e5f607182930"))
                .isInstanceOf(NoSuchElementException.class).hasMessageContaining("No existe la foto");
    }

    @Test
    void devuelveElContenidoConSuTipoYLongitud() throws Exception {
        GridFSFile archivo = mock(GridFSFile.class);
        when(archivo.getLength()).thenReturn(10L);
        GridFsResource recurso = mock(GridFsResource.class);
        when(recurso.getContentType()).thenReturn("image/png");
        when(recurso.getInputStream()).thenReturn(new ByteArrayInputStream(PNG));
        when(gridFs.findOne(any(Query.class))).thenReturn(archivo);
        when(gridFs.getResource(any(GridFSFile.class))).thenReturn(recurso);

        FotoRecuperada foto = servicio.obtener("6730a1f2c3d4e5f607182930");

        assertThat(foto.contentType()).isEqualTo("image/png");
        assertThat(foto.longitud()).isEqualTo(10L);
        assertThat(foto.contenido().readAllBytes()).isEqualTo(PNG);
    }

    @Test
    void siFaltaElTipoGuardadoSeSirveComoBinarioGenerico() throws Exception {
        GridFSFile archivo = mock(GridFSFile.class);
        GridFsResource recurso = mock(GridFsResource.class);
        when(recurso.getContentType()).thenThrow(new IllegalStateException("sin contentType"));
        when(recurso.getInputStream()).thenReturn(new ByteArrayInputStream(PNG));
        when(gridFs.findOne(any(Query.class))).thenReturn(archivo);
        when(gridFs.getResource(any(GridFSFile.class))).thenReturn(recurso);

        assertThat(servicio.obtener("6730a1f2c3d4e5f607182930").contentType()).isEqualTo("application/octet-stream");
    }
}
