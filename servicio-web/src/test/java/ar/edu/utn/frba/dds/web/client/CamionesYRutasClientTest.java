package ar.edu.utn.frba.dds.web.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import ar.edu.utn.frba.dds.web.client.logistica.CamionApi;
import ar.edu.utn.frba.dds.web.client.logistica.CamionRequest;
import ar.edu.utn.frba.dds.web.client.logistica.CamionesClient;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutasClient;
import ar.edu.utn.frba.dds.web.error.BackendRechazoException;
import ar.edu.utn.frba.dds.web.error.RecursoNoEncontradoException;
import ar.edu.utn.frba.dds.web.error.ServicioBackend;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class CamionesYRutasClientTest {

    private MockRestServiceServer servidor;
    private CamionesClient camiones;
    private RutasClient rutas;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://logistica");
        servidor = MockRestServiceServer.bindTo(builder).build();
        RestClient http = ManejoErroresApi.configurar(builder, ServicioBackend.LOGISTICA, PropagadorCredenciales.NINGUNO).build();
        camiones = new CamionesClient(http);
        rutas = new RutasClient(http);
    }

    @Test
    void listaLosCamiones() {
        servidor.expect(requestTo("http://logistica/api/camiones"))
                .andRespond(withSuccess("""
                        [{"idCamion": 1, "patente": "AA123BB", "capacidadVolumenM3": 20, "alturaM": 3,
                          "capacidadCargaKg": 1500, "estadoCamion": "DISPONIBLE"}]
                        """, MediaType.APPLICATION_JSON));

        List<CamionApi> lista = camiones.camiones();

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).estadoEditable()).isTrue();
    }

    @Test
    void soloSeEditaElEstadoDeLosCamionesDisponiblesOFueraDeServicio() {
        assertThat(new CamionApi(1, "A", 1, 1, 1, "EN_RUTA").estadoEditable()).isFalse();
        assertThat(new CamionApi(1, "A", 1, 1, 1, "ASIGNADO").estadoEditable()).isFalse();
        assertThat(new CamionApi(1, "A", 1, 1, 1, "FUERA_DE_SERVICIO").estadoEditable()).isTrue();
    }

    @Test
    void creaUnCamionSinEnviarElEstadoSiEsNull() {
        servidor.expect(requestTo("http://logistica/api/camiones"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.patente").value("AA123BB"))
                .andExpect(jsonPath("$.estadoCamion").doesNotExist())
                .andRespond(withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"idCamion\": 4, \"patente\": \"AA123BB\"}"));

        CamionApi creado = camiones.crear(new CamionRequest("AA123BB", 20, 3, 1500, null));

        assertThat(creado.idCamion()).isEqualTo(4);
        servidor.verify();
    }

    @Test
    void actualizaYElimina() {
        servidor.expect(requestTo("http://logistica/api/camiones/4"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess("{\"idCamion\": 4}", MediaType.APPLICATION_JSON));
        servidor.expect(requestTo("http://logistica/api/camiones/4"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withNoContent());

        camiones.actualizar(4, new CamionRequest("AA123BB", 20, 3, 1500, "DISPONIBLE"));
        camiones.eliminar(4);

        servidor.verify();
    }

    @Test
    void unPatenteDuplicadoLlegaComoRechazoConElMensajeDeLogistica() {
        servidor.expect(requestTo("http://logistica/api/camiones"))
                .andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\": \"El dato viola una restricción de la base (valor duplicado o referencia inválida)\"}"));

        assertThatThrownBy(() -> camiones.crear(new CamionRequest("AA123BB", 20, 3, 1500, null)))
                .isInstanceOf(BackendRechazoException.class)
                .hasMessageContaining("duplicado");
    }

    @Test
    void leeUnaRutaConSusParadasYEntregasSinElDniDelChofer() {
        servidor.expect(requestTo("http://logistica/api/rutas/7"))
                .andRespond(withSuccess("""
                        {"idRuta": 7, "fecha": "2026-11-12", "estadoRuta": "INICIADA",
                         "camionAsociado": {"idCamion": 1, "patente": "AA123BB", "estadoCamion": "EN_RUTA"},
                         "chofer": {"idChofer": 2, "nombre": "Juan Pérez", "dni": 30111222, "habilitado": true},
                         "paradas": [{"idParada": 1, "numeroParada": 1, "idEntidadBeneficiariaAsociada": 3,
                                      "direccion": {"calle": "Av. Siempre Viva", "numero": "742",
                                                    "ciudad": {"nombre": "Escobar", "provincia": {"nombre": "Buenos Aires"}}},
                                      "entregas": [{"idEntrega": 9, "idDonacionAsociada": 15, "estadoEntrega": "EN_TRASLADO",
                                                    "camionEntrega": {"patente": "AA123BB"}, "fotos": []},
                                                   {"idEntrega": 10, "idDonacionAsociada": 16, "estadoEntrega": "ENTREGADA",
                                                    "fechaHoraEntrega": "2026-11-12T17:30:00",
                                                    "fotos": [{"url": "https://ejemplo.org/f.jpg", "fecha": "2026-11-12"}]}]}]}
                        """, MediaType.APPLICATION_JSON));

        RutaApi ruta = rutas.ruta(7);

        assertThat(ruta.chofer().nombre()).isEqualTo("Juan Pérez");
        assertThat(ruta.totalEntregas()).isEqualTo(2);
        assertThat(ruta.entregadas()).isEqualTo(1);
        assertThat(ruta.resueltas()).isEqualTo(1);
        assertThat(ruta.paradas().get(0).direccion().texto()).isEqualTo("Av. Siempre Viva 742, Escobar");
    }

    @Test
    void iniciarUnaRutaQueNoEstaPlanificadaLlegaComoRechazo() {
        servidor.expect(requestTo("http://logistica/api/rutas/7/iniciar"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\": \"La ruta 7 no puede iniciarse estando en estado INICIADA\"}"));

        assertThatThrownBy(() -> rutas.iniciar(7))
                .isInstanceOf(BackendRechazoException.class)
                .hasMessageContaining("no puede iniciarse");
    }

    @Test
    void unaRutaInexistenteEsUn404() {
        servidor.expect(requestTo("http://logistica/api/rutas/99"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\": \"No existe la ruta 99\"}"));

        assertThatThrownBy(() -> rutas.ruta(99)).isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void planificarDevuelveLasRutasNuevas() {
        servidor.expect(requestTo("http://logistica/api/planificador/ejecutar"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("[{\"idRuta\": 8, \"estadoRuta\": \"PLANIFICADA\", \"paradas\": []}]",
                        MediaType.APPLICATION_JSON));

        assertThat(rutas.planificar()).hasSize(1);
    }
}
