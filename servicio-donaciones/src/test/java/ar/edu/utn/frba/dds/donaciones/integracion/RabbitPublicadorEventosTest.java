package ar.edu.utn.frba.dds.donaciones.integracion;
import ar.edu.utn.frba.dds.donaciones.config.BandejaNotificaciones;
import ar.edu.utn.frba.dds.donaciones.domain.personas.*;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.*;
import ar.edu.utn.frba.dds.donaciones.dto.CambioEstadoDTO;
import ar.edu.utn.frba.dds.donaciones.DatosPrueba;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class RabbitPublicadorEventosTest {
 @Test void bienvenidaUsaEmailAunqueWhatsappSeaPreferido() {
    var bandeja=mock(BandejaNotificaciones.class);var publicador=new RabbitPublicadorEventos(bandeja);
    var persona=new PersonaHumana("Ana","Perez",30,"123",null);
    persona.agregarMedio(new MedioContacto(TipoContacto.EMAIL,"ana@example.org",false));
    persona.agregarMedio(new MedioContacto(TipoContacto.WHATSAPP,"3515551234",true));
    publicador.publicar("BIENVENIDA_DONANTE",new Donante(1L,persona));
    var cap=ArgumentCaptor.forClass(Object.class);verify(bandeja).guardar(cap.capture());
    var datos=new ObjectMapper().valueToTree(cap.getValue());
    assertThat(datos.get("medio").asText()).isEqualTo("EMAIL");assertThat(datos.get("contacto").asText()).isEqualTo("ana@example.org");
 }
 @Test void comprobanteInicioYFalloIncluyenDatosYAdministradores() {
    var bandeja=mock(BandejaNotificaciones.class);var publicador=new RabbitPublicadorEventos(bandeja);
    ReflectionTestUtils.setField(publicador,"administradores","admin@example.org");
    var persona=new PersonaHumana("Ana","Perez",30,"123",null);persona.agregarMedio(new MedioContacto(TipoContacto.EMAIL,"ana@example.org",true));
    var juridica=new PersonaJuridica("Comedor",TipoOrganizacion.ONG,"Asistencia",null);juridica.agregarMedio(new MedioContacto(TipoContacto.EMAIL,"comedor@example.org",true));
    var donacion=DatosPrueba.donacion(1L,DatosPrueba.item(1L,"Silla",DatosPrueba.subcategoria("silla"),1,null),1);
    donacion.setDonante(new Donante(1L,persona));donacion.setEntidadBeneficiaria(new EntidadBeneficiaria(1L,juridica,"Comedor"));
    var contexto=new CambioEstadoDTO();contexto.setEventoId("logistica-1");contexto.setSeguimientoUrl("http://localhost:8083/api/rutas/1/seguimiento");
    contexto.setFechaHoraEntrega(java.time.LocalDateTime.of(2026,10,9,12,30));contexto.setPatenteCamion("ABC123");
    publicador.publicar("RUTA_INICIADA",donacion,contexto);
    publicador.publicar("ENTREGA_CONFIRMADA",donacion,contexto);
    publicador.publicar("ENTREGA_FALLIDA",donacion,contexto);
    var cap=ArgumentCaptor.forClass(Object.class);verify(bandeja,times(7)).guardar(cap.capture());
    var mapper=new ObjectMapper();var datos=cap.getAllValues().stream().map(v -> mapper.<com.fasterxml.jackson.databind.JsonNode>valueToTree(v)).toList();
    assertThat(datos.get(0).get("mensaje").asText()).contains("/seguimiento");
    assertThat(datos.get(2).get("mensaje").asText()).contains("ABC123","2026-10-09T12:30");
    assertThat(datos.get(6).get("contacto").asText()).isEqualTo("admin@example.org");
 }
}
