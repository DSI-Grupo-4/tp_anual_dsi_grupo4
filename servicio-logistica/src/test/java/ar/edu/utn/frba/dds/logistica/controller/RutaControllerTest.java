package ar.edu.utn.frba.dds.logistica.controller;
import ar.edu.utn.frba.dds.logistica.domain.rutas.*;
import ar.edu.utn.frba.dds.logistica.domain.eventos.GestorEventos;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDate;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class RutaControllerTest {
 @Test void inicioIncluyeSeguimientoYRecepcionGuardaFechaHoraCamion() {
    var camion=new Camion(1,"ABC123",20,3,1000,EstadoCamion.DISPONIBLE);
    var entrega=new Entrega(1,1,1,new Direccion("<script>alert(1)</script>","1",null),LocalDate.now(),10,1,1);entrega.asignarARuta(camion);
    var parada=new Parada(1,1,entrega.getDireccionDestino(),List.of(entrega));
    var ruta=new Ruta(1,camion,null,LocalDate.now(),List.of(parada));
    var gestor=mock(GestorRutas.class);when(gestor.buscarPorId(1)).thenReturn(ruta);
    var controller=new RutaController(gestor,mock(GestorEventos.class));ReflectionTestUtils.setField(controller,"publicBaseUrl","http://localhost:8083");
    controller.iniciarRuta(1);assertThat(entrega.getSeguimientoUrl()).isEqualTo("http://localhost:8083/api/rutas/1/seguimiento");
    assertThat(controller.seguimiento(1)).contains("EN_TRASLADO","&lt;script&gt;").doesNotContain("<script>");
    parada.confirmarRecepcion(null);assertThat(entrega.getFechaHoraEntrega()).isNotNull();assertThat(entrega.getCamionEntrega().getPatente()).isEqualTo("ABC123");
    assertThat(controller.seguimiento(1)).contains("ENTREGADA");
 }
}
