package ar.edu.utn.frba.dds.incentivos.controller;
import ar.edu.utn.frba.dds.incentivos.donante.GestorDonante;
import ar.edu.utn.frba.dds.incentivos.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.assertj.core.api.Assertions.*;
class PerfilDonanteTest {
 private MockMvc mvc(IncentivosController c){return MockMvcBuilders.standaloneSetup(c).setControllerAdvice(new GlobalExceptionHandler()).build();}
 @Test void perfilTieneMisionesInicialesSinSumarDonacionesYRepetirloActualizaDatos() throws Exception {
    var c=new IncentivosController();var mvc=mvc(c);
    String perfil="{\"nombre\":\"Ana Perez\",\"medioContactoPreferido\":\"EMAIL\",\"contactoPreferido\":\"ana@example.org\"}";
    mvc.perform(put("/api/donantes/730001/perfil").contentType("application/json").content(perfil)).andExpect(status().isNoContent());
    mvc.perform(get("/api/donantes/730001/misiones")).andExpect(status().isOk()).andExpect(jsonPath("$[0].activa").value(true)).andExpect(jsonPath("$[0].progresoActual").value(0));
    mvc.perform(get("/api/donantes/730001/metricas")).andExpect(status().isOk()).andExpect(jsonPath("$.solicitudesDonacionHechas").value(0)).andExpect(jsonPath("$.impactoAcumulado").value(0));
    mvc.perform(get("/api/donantes/730001/insignias")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    mvc.perform(put("/api/donantes/730001/perfil").contentType("application/json").content(perfil.replace("Ana Perez","Ana Nueva").replace("ana@example.org","nueva@example.org"))).andExpect(status().isNoContent());
    var d=GestorDonante.getInstance().buscarDonante(730001L);assertThat(d.getNombre()).isEqualTo("Ana Nueva");assertThat(d.getContactoPreferido()).isEqualTo("nueva@example.org");assertThat(d.getSolicitudesDonacionHechas()).isZero();
 }
 @Test void recuperaDesdeDonacionesUnDonanteCargadoAntesYNoInventaIdsInexistentes() throws Exception {
    var c=new IncentivosController();var builder=RestClient.builder().baseUrl("http://donaciones");var servidor=MockRestServiceServer.bindTo(builder).build();
    ReflectionTestUtils.setField(c,"donacionesClient",builder.build());var mvc=mvc(c);
    servidor.expect(requestTo("http://donaciones/api/donantes/730002")).andRespond(withSuccess("{\"id\":730002,\"tipo\":\"HUMANA\",\"nombre\":\"Ana\",\"apellido\":\"Perez\",\"mediosContacto\":[{\"tipo\":\"TELEFONO\",\"valor\":\"3515551234\",\"esPreferido\":true}]}",org.springframework.http.MediaType.APPLICATION_JSON));
    servidor.expect(requestTo("http://donaciones/api/donantes/730003")).andRespond(withResourceNotFound());
    mvc.perform(get("/api/donantes/730002/misiones")).andExpect(status().isOk()).andExpect(jsonPath("$[0].progresoActual").value(0));
    assertThat(GestorDonante.getInstance().buscarDonante(730002L).getMedioContactoPreferido()).isEqualTo("SMS");
    mvc.perform(get("/api/donantes/730003/misiones")).andExpect(status().isNotFound());servidor.verify();
    assertThatThrownBy(()->GestorDonante.getInstance().buscarDonante(730003L)).isInstanceOf(java.util.NoSuchElementException.class);
 }
}
