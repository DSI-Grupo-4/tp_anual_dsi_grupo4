package ar.edu.utn.frba.dds.web.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import ar.edu.utn.frba.dds.web.client.logistica.EntregaAdminApi;
import ar.edu.utn.frba.dds.web.client.logistica.EntregasClient;
import ar.edu.utn.frba.dds.web.client.logistica.ReingresoApi;
import ar.edu.utn.frba.dds.web.error.BackendRechazoException;
import ar.edu.utn.frba.dds.web.error.ServicioBackend;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class EntregasClientTest {

    private MockRestServiceServer servidor;
    private EntregasClient entregas;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://logistica");
        servidor = MockRestServiceServer.bindTo(builder).build();
        entregas = new EntregasClient(
                ManejoErroresApi.configurar(builder, ServicioBackend.LOGISTICA, PropagadorCredenciales.NINGUNO).build());
    }

    @Test
    void listaLasEntregasConSuMotivoIgnorandoLoQueNoUsa() {
        servidor.expect(requestTo("http://logistica/api/entregas"))
                .andRespond(withSuccess("""
                        [{"idEntrega": 5, "idDonacionAsociada": 105, "idEntidadBeneficiariaAsociada": 3,
                          "estadoEntrega": "NO_RECIBIDA", "fecha": "2026-11-12", "justificacionFallo": "Nadie atendió",
                          "direccionDestino": {"calle": "Calle"}, "camionEntrega": {"patente": "AA123BB"}, "fotos": []}]
                        """, MediaType.APPLICATION_JSON));

        List<EntregaAdminApi> lista = entregas.entregas();

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).justificacionFallo()).isEqualTo("Nadie atendió");
        assertThat(lista.get(0).puedeReingresar()).isTrue();
    }

    @Test
    void soloLasNoRecibidasYLasFallidasPuedenReingresar() {
        assertThat(new EntregaAdminApi(1, 1, 1, "FALLIDA", null, null, null).puedeReingresar()).isTrue();
        assertThat(new EntregaAdminApi(1, 1, 1, "ENTREGADA", null, null, null).puedeReingresar()).isFalse();
        assertThat(new EntregaAdminApi(1, 1, 1, "EN_TRASLADO", null, null, null).puedeReingresar()).isFalse();
        assertThat(new EntregaAdminApi(1, 1, 1, "REPLANIFICABLE", null, null, null).puedeReingresar()).isFalse();
    }

    @Test
    void reingresarDevuelveElNuevoEstado() {
        servidor.expect(requestTo("http://logistica/api/entregas/5/reingresar"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"idEntrega\": 5, \"estadoEntrega\": \"REPLANIFICABLE\"}", MediaType.APPLICATION_JSON));

        ReingresoApi resultado = entregas.reingresar(5);

        assertThat(resultado.estadoEntrega()).isEqualTo("REPLANIFICABLE");
        servidor.verify();
    }

    @Test
    void reingresarUnaEntregaQueNoFalloLlegaComoRechazo() {
        servidor.expect(requestTo("http://logistica/api/entregas/5/reingresar"))
                .andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\": \"La entrega 5 no puede reingresar al depósito estando en estado ENTREGADA\"}"));

        assertThatThrownBy(() -> entregas.reingresar(5))
                .isInstanceOf(BackendRechazoException.class).hasMessageContaining("no puede reingresar");
    }
}
