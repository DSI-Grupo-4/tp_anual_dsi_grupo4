package ar.edu.utn.frba.dds.web.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import ar.edu.utn.frba.dds.web.client.logistica.EntregaApi;
import ar.edu.utn.frba.dds.web.client.logistica.LogisticaClient;
import ar.edu.utn.frba.dds.web.error.BackendException;
import ar.edu.utn.frba.dds.web.error.BackendNoDisponibleException;
import ar.edu.utn.frba.dds.web.error.BackendRechazoException;
import ar.edu.utn.frba.dds.web.error.RecursoNoEncontradoException;
import ar.edu.utn.frba.dds.web.error.ServicioBackend;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class ManejoErroresApiTest {

    private MockRestServiceServer servidor;
    private LogisticaClient cliente;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://logistica");
        servidor = MockRestServiceServer.bindTo(builder).build();
        PropagadorCredenciales credenciales = headers -> headers.setBearerAuth("token-de-prueba");
        cliente = new LogisticaClient(ManejoErroresApi.configurar(builder, ServicioBackend.LOGISTICA, credenciales).build());
    }

    @Test
    void agregaLasCredencialesYNoLeeDireccionNiCamion() {
        servidor.expect(requestTo("http://logistica/api/entregas"))
                .andExpect(header("Authorization", "Bearer token-de-prueba"))
                .andRespond(withSuccess("""
                        [{"idEntrega": 7, "idDonacionAsociada": 15, "estadoEntrega": "ENTREGADA",
                          "fechaHoraEntrega": "2026-11-03T17:30:00",
                          "direccionDestino": {"calle": "Av. Siempre Viva", "numero": "742"},
                          "camionEntrega": {"patente": "AB123CD"},
                          "fotos": [{"url": "https://ejemplo.org/f.jpg", "fecha": "2026-11-03"}]}]
                        """, MediaType.APPLICATION_JSON));

        List<EntregaApi> entregas = cliente.entregas();

        assertThat(entregas).singleElement().satisfies(e -> {
            assertThat(e.idEntrega()).isEqualTo(7);
            assertThat(e.fechaHoraEntrega()).isEqualTo(LocalDateTime.of(2026, 11, 3, 17, 30));
            assertThat(e.fotos()).extracting(EntregaApi.FotoApi::url).containsExactly("https://ejemplo.org/f.jpg");
            assertThat(e.toString()).doesNotContain("Siempre Viva", "AB123CD");
        });
    }

    @Test
    void un503EsServicioNoDisponible() {
        servidor.expect(requestTo("http://logistica/api/entregas")).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(cliente::entregas)
                .isInstanceOf(BackendNoDisponibleException.class)
                .hasMessageContaining("Logística");
    }

    @Test
    void un404EsRecursoNoEncontradoConElMensajeDelBackend() {
        servidor.expect(requestTo("http://logistica/api/entregas"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\": \"No existe la entrega 99\"}"));

        assertThatThrownBy(cliente::entregas)
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("No existe la entrega 99");
    }

    @Test
    void un400TraeLosErroresPorCampo() {
        servidor.expect(requestTo("http://logistica/api/entregas"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\": \"Datos inválidos\", \"detalles\": {\"patente\": \"es obligatoria\"}}"));

        assertThatThrownBy(cliente::entregas)
                .isInstanceOfSatisfying(BackendRechazoException.class, e -> {
                    assertThat(e.getMessage()).isEqualTo("Datos inválidos");
                    assertThat(e.getErroresPorCampo()).containsEntry("patente", "es obligatoria");
                });
    }

    @Test
    void un500SinCuerpoEsErrorGenerico() {
        servidor.expect(requestTo("http://logistica/api/entregas")).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(cliente::entregas)
                .isExactlyInstanceOf(BackendException.class)
                .satisfies(e -> assertThat(((BackendException) e).getStatus()).isEqualTo(500));
    }

    @Test
    void unaConexionRechazadaEsServicioNoDisponible() {
        // Puerto 1: nadie escucha, la conexión se rechaza enseguida.
        RestClient real = ManejoErroresApi.configurar(RestClient.builder().baseUrl("http://127.0.0.1:1"),
                ServicioBackend.LOGISTICA, PropagadorCredenciales.NINGUNO).build();

        assertThatThrownBy(() -> new LogisticaClient(real).entregas())
                .isInstanceOf(BackendNoDisponibleException.class);
    }
}
