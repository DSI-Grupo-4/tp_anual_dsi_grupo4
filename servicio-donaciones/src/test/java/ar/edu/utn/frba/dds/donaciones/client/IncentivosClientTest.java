package ar.edu.utn.frba.dds.donaciones.client;
import ar.edu.utn.frba.dds.donaciones.DatosPrueba;
import ar.edu.utn.frba.dds.donaciones.domain.personas.*;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import java.nio.file.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class IncentivosClientTest {
 @TempDir Path dir;
 @Test void guardaAltaYEntregaYReintentaEnOrdenSinBloquearElAlta() throws Exception {
   var client=new IncentivosClient("http://localhost:18081");ReflectionTestUtils.setField(client,"pendientes",dir);
   var rest=mock(RestClient.class);when(rest.post()).thenThrow(new IllegalStateException("offline"));ReflectionTestUtils.setField(client,"restClient",rest);
   var donacion=DatosPrueba.donacion(1L,DatosPrueba.item(1L,"Silla",DatosPrueba.subcategoria("silla"),1,null),1);
   var persona=new PersonaHumana("Ana","Perez",30,"123",null);persona.agregarMedio(new MedioContacto(TipoContacto.EMAIL,"ana@example.org",true));donacion.setDonante(new Donante(1L,persona));
   client.registrarActividadDonacion(donacion);verifyNoInteractions(rest);
   donacion.cambiarEstado(EstadoTrack.ASIGNACION_REALIZADA,null);donacion.cambiarEstado(EstadoTrack.LISTA_PARA_ENTREGAR,null);donacion.cambiarEstado(EstadoTrack.EN_TRASLADO,null);donacion.cambiarEstado(EstadoTrack.ENTREGADA,null);
   client.registrarActividadDonacion(donacion);client.enviarPendientes();
   try(var archivos=Files.list(dir)){assertThat(archivos.filter(p->p.toString().endsWith(".json")).count()).isEqualTo(2);}
   var recuperado=new IncentivosClient("http://localhost:18081");ReflectionTestUtils.setField(recuperado,"pendientes",dir);
   var disponible=mock(RestClient.class,RETURNS_DEEP_STUBS);ReflectionTestUtils.setField(recuperado,"restClient",disponible);
   recuperado.enviarPendientes();
   try(var archivos=Files.list(dir)){assertThat(archivos.count()).isZero();}
   verify(disponible,times(2)).post();
 }
}
