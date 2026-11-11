package ar.edu.utn.frba.dds.web.admin;
import ar.edu.utn.frba.dds.web.client.donaciones.*;
import ar.edu.utn.frba.dds.web.client.ManejoErroresApi;
import ar.edu.utn.frba.dds.web.error.*;
import org.junit.jupiter.api.*;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import java.util.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
class AdminDonacionesClientTest {
    MockRestServiceServer server; AdminDonacionesClient api;
    @BeforeEach void preparar() {
        var builder=RestClient.builder().baseUrl("http://donaciones"); server=MockRestServiceServer.bindTo(builder).build();
        var http=ManejoErroresApi.configurar(builder,ServicioBackend.DONACIONES,h->h.setBearerAuth("token-de-prueba")).build();
        api=new AdminDonacionesClient(http,http);
    }
    @AfterEach void verificar() { server.verify(); }
    @Test void paginaCodificaBusquedaYPropagaToken() {
        server.expect(requestTo("http://donaciones/api/administracion/donantes?q=Ana%26activo%3Dfalse"))
            .andExpect(header("Authorization","Bearer token-de-prueba"))
            .andRespond(withSuccess("{\"content\":[],\"totalElements\":0,\"totalPages\":0,\"number\":0}",MediaType.APPLICATION_JSON));
        assertThat(api.donantes(Map.of("q","Ana&activo=false")).content()).isEmpty();
    }
    @Test void altaYEdicionEnvianJSONAlEndpointCorrespondiente() {
        server.expect(requestTo("http://donaciones/api/donantes/humanos")).andExpect(method(HttpMethod.POST))
            .andExpect(content().json("{\"nombre\":\"Ana\"}")).andRespond(withSuccess("{\"id\":1}",MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://donaciones/api/donantes/1")).andExpect(method(HttpMethod.PUT)).andRespond(withSuccess("{\"id\":1}",MediaType.APPLICATION_JSON));
        assertThat(api.crearDonante("HUMANA",Map.of("nombre","Ana")).id()).isEqualTo(1);
        api.editarDonante(1,Map.of("nombre","Ana"));
    }
    @Test void importacionUsaMultipartConNombreArchivo() {
        server.expect(requestTo("http://donaciones/api/donantes/importar")).andExpect(method(HttpMethod.POST))
            .andExpect(header("Content-Type",containsString("multipart/form-data")))
            .andExpect(content().string(containsString("filename=\"donantes.csv\"")))
            .andRespond(withSuccess("{\"cantidadImportados\":10001}",MediaType.APPLICATION_JSON));
        assertThat(api.importar(new MockMultipartFile("archivo","donantes.csv","text/csv","datos".getBytes())).cantidadImportados()).isEqualTo(10001);
    }
    @Test void ejecutarYConsultarUltimoNoSeConfunden() {
        String vacio="{\"interseccion\":[],\"porCompatibilidad\":[],\"porSubatencion\":[]}";
        server.expect(requestTo("http://donaciones/api/donaciones/3/candidatas/ultimo")).andExpect(method(HttpMethod.GET)).andRespond(withSuccess(vacio,MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://donaciones/api/donaciones/3/candidatas")).andExpect(method(HttpMethod.GET)).andRespond(withSuccess(vacio,MediaType.APPLICATION_JSON));
        api.ultimoResultado(3); api.ejecutar(3);
    }
    @Test void asignacionYVencimientoTienenPayloadAcotado() {
        server.expect(requestTo("http://donaciones/api/donaciones/3/asignar")).andExpect(method(HttpMethod.POST))
            .andExpect(content().json("{\"entidadId\":2}",true)).andRespond(withSuccess());
        server.expect(requestTo("http://donaciones/api/donaciones/3/estado")).andExpect(method(HttpMethod.PATCH))
            .andExpect(content().json("{\"nuevoEstado\":\"VENCIDA\",\"justificacion\":\"Vencimiento alcanzado\"}",true)).andRespond(withSuccess());
        api.asignar(3,2); api.vencer(3,"Vencimiento alcanzado");
    }
    @Test void operacionesDeBajaYReactivacionUsanVerbosCorrectos() {
        server.expect(requestTo("http://donaciones/api/donantes/1")).andExpect(method(HttpMethod.DELETE)).andRespond(withNoContent());
        server.expect(requestTo("http://donaciones/api/donantes/1/reactivar")).andExpect(method(HttpMethod.PATCH)).andRespond(withNoContent());
        server.expect(requestTo("http://donaciones/api/entidades/2")).andExpect(method(HttpMethod.DELETE)).andRespond(withNoContent());
        server.expect(requestTo("http://donaciones/api/entidades/2/reactivar")).andExpect(method(HttpMethod.PATCH)).andRespond(withNoContent());
        server.expect(requestTo("http://donaciones/api/donaciones/3")).andExpect(method(HttpMethod.DELETE)).andRespond(withNoContent());
        api.bajaDonante(1); api.reactivarDonante(1); api.bajaEntidad(2); api.reactivarEntidad(2); api.eliminarDonacion(3);
    }
    @Test void conflictoDeAsignacionLlegaAlFormulario() {
        server.expect(requestTo("http://donaciones/api/donaciones/3/asignar")).andRespond(withStatus(HttpStatus.CONFLICT).body("{\"error\":\"Ya no está en depósito\"}").contentType(MediaType.APPLICATION_JSON));
        assertThatThrownBy(()->api.asignar(3,2)).isInstanceOf(BackendRechazoException.class).hasMessageContaining("depósito");
    }
}
