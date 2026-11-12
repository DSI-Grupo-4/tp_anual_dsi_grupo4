package ar.edu.utn.frba.dds.web.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import ar.edu.utn.frba.dds.web.client.donaciones.NecesidadApi;
import ar.edu.utn.frba.dds.web.client.donaciones.NecesidadRequest;
import ar.edu.utn.frba.dds.web.client.donaciones.NecesidadesClient;
import ar.edu.utn.frba.dds.web.error.BackendRechazoException;
import ar.edu.utn.frba.dds.web.error.ServicioBackend;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class NecesidadesClientTest {

    private MockRestServiceServer servidor;
    private NecesidadesClient cliente;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://donaciones");
        servidor = MockRestServiceServer.bindTo(builder).build();
        cliente = new NecesidadesClient(
                ManejoErroresApi.configurar(builder, ServicioBackend.DONACIONES, PropagadorCredenciales.NINGUNO).build());
    }

    @Test
    void listaLasNecesidadesDeLaEntidadIgnorandoLosCamposQueNoUsa() {
        servidor.expect(requestTo("http://donaciones/api/entidades/3/necesidades"))
                .andRespond(withSuccess("""
                        [{"id": 5, "tipo": "RECURRENTE", "descripcion": "Leche en polvo", "subcategoria": "FIDEOS_SECOS",
                          "unidadMedida": "KILOGRAMO", "cantidadRequerida": 50, "cantidadRecibida": 20,
                          "satisfecha": false, "entidadBeneficiariaId": 3, "periodicidad": "MENSUAL", "campoNuevo": 1}]
                        """, MediaType.APPLICATION_JSON));

        List<NecesidadApi> necesidades = cliente.necesidades(3L);

        assertThat(necesidades).hasSize(1);
        NecesidadApi necesidad = necesidades.get(0);
        assertThat(necesidad.recurrente()).isTrue();
        assertThat(necesidad.pendiente()).isEqualByComparingTo("30");
        assertThat(necesidad.urgente()).isFalse();
        servidor.verify();
    }

    @Test
    void creaUnaRecurrenteEnSuEndpointYSoloEnviaSusCampos() {
        servidor.expect(requestTo("http://donaciones/api/entidades/3/necesidades/recurrentes"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.periodicidad").value("MENSUAL"))
                .andExpect(jsonPath("$.tipoExtraordinario").doesNotExist())
                .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"id\": 9, \"tipo\": \"RECURRENTE\"}"));

        NecesidadApi creada = cliente.crear(3L, new NecesidadRequest("RECURRENTE", "Leche", "FIDEOS_SECOS",
                "KILOGRAMO", new BigDecimal("10"), "MENSUAL", null));

        assertThat(creada.id()).isEqualTo(9L);
        servidor.verify();
    }

    @Test
    void creaUnaExtraordinariaEnSuEndpoint() {
        servidor.expect(requestTo("http://donaciones/api/entidades/3/necesidades/extraordinarias"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.tipoExtraordinario").value("INUNDACION"))
                .andExpect(jsonPath("$.periodicidad").doesNotExist())
                .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"id\": 10, \"tipo\": \"EXTRAORDINARIA\"}"));

        cliente.crear(3L, new NecesidadRequest("EXTRAORDINARIA", "Frazadas", "FRAZADA",
                "UNIDAD", new BigDecimal("100"), null, "INUNDACION"));

        servidor.verify();
    }

    @Test
    void actualizaEnviandoElTipo() {
        servidor.expect(requestTo("http://donaciones/api/entidades/3/necesidades/5"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(jsonPath("$.tipo").value("RECURRENTE"))
                .andRespond(withSuccess("{\"id\": 5}", MediaType.APPLICATION_JSON));

        cliente.actualizar(3L, 5L, new NecesidadRequest("RECURRENTE", "Leche", "FIDEOS_SECOS",
                "KILOGRAMO", new BigDecimal("60"), "MENSUAL", null));

        servidor.verify();
    }

    @Test
    void eliminaPorSuId() {
        servidor.expect(requestTo("http://donaciones/api/entidades/3/necesidades/5"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withNoContent());

        cliente.eliminar(3L, 5L);

        servidor.verify();
    }

    @Test
    void unRechazoDeDonacionesLlegaConSuMensaje() {
        servidor.expect(requestTo("http://donaciones/api/entidades/3/necesidades/5"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\": \"La necesidad tiene donaciones asignadas\"}"));

        assertThatThrownBy(() -> cliente.eliminar(3L, 5L))
                .isInstanceOf(BackendRechazoException.class)
                .hasMessageContaining("donaciones asignadas");
    }
}
