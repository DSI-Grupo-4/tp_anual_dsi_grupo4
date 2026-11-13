package ar.edu.utn.frba.dds.web.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import ar.edu.utn.frba.dds.web.client.logistica.FotoSubidaApi;
import ar.edu.utn.frba.dds.web.client.logistica.FotosClient;
import ar.edu.utn.frba.dds.web.error.BackendRechazoException;
import ar.edu.utn.frba.dds.web.error.ServicioBackend;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class FotosClientTest {

    private MockRestServiceServer servidor;
    private FotosClient fotos;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://logistica");
        servidor = MockRestServiceServer.bindTo(builder).build();
        fotos = new FotosClient(ManejoErroresApi.configurar(builder, ServicioBackend.LOGISTICA, PropagadorCredenciales.NINGUNO).build());
    }

    @Test
    void subeElArchivoComoMultipartEnLaParteFileConSuNombreYSuTipo() {
        servidor.expect(requestTo("http://logistica/api/fotos"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
                .andExpect(content().string(allOf(
                        containsString("name=\"file\""),
                        containsString("filename=\"entrega.png\""),
                        containsString("Content-Type: image/png"))))
                .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"id\": \"abc123\", \"url\": \"http://logistica/api/fotos/abc123\"}"));

        FotoSubidaApi subida = fotos.subir("entrega.png", "image/png", new byte[] {1, 2, 3});

        assertThat(subida.id()).isEqualTo("abc123");
        assertThat(subida.url()).isEqualTo("http://logistica/api/fotos/abc123");
        servidor.verify();
    }

    @Test
    void sinNombreNiTipoUsaValoresPorDefectoEnVezDeFallar() {
        servidor.expect(requestTo("http://logistica/api/fotos"))
                .andExpect(content().string(allOf(containsString("filename=\"foto\""),
                        containsString("Content-Type: application/octet-stream"))))
                .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"id\": \"1\", \"url\": \"http://l/1\"}"));

        fotos.subir(null, "esto no es un tipo", new byte[] {1});

        servidor.verify();
    }

    @Test
    void siLogisticaRechazaElArchivoLlegaComoRechazoConSuMensaje() {
        servidor.expect(requestTo("http://logistica/api/fotos"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\": \"El contenido del archivo no es una imagen JPEG, PNG o WebP válida\"}"));

        assertThatThrownBy(() -> fotos.subir("a.jpg", "image/jpeg", new byte[] {1}))
                .isInstanceOf(BackendRechazoException.class).hasMessageContaining("no es una imagen");
    }

    @Test
    void unaFotoDemasiadoGrandeSeTrataComoRechazo() {
        servidor.expect(requestTo("http://logistica/api/fotos"))
                .andRespond(withStatus(HttpStatus.PAYLOAD_TOO_LARGE).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\": \"La foto supera el tamaño máximo de 5 MB\"}"));

        assertThatThrownBy(() -> fotos.subir("a.jpg", "image/jpeg", new byte[] {1}))
                .isInstanceOf(BackendRechazoException.class).hasMessageContaining("5 MB");
    }
}
