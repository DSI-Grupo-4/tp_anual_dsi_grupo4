package ar.edu.utn.frba.dds.donaciones.scheduler;
import ar.edu.utn.frba.dds.donaciones.dto.*;
import ar.edu.utn.frba.dds.donaciones.integracion.LogisticaBroker;
import ar.edu.utn.frba.dds.donaciones.service.DonacionService;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
class EventosLogisticaSchedulerTest {
 @Test void noConfirmaEventoSiFallaPeroReintentaEnLaSiguienteConsulta() {
    var broker=mock(LogisticaBroker.class); var servicio=mock(DonacionService.class);
    var evento=new EventoLogisticoDTO(); evento.setIdEvento(UUID.randomUUID());evento.setTipoEvento("RUTA_INICIADA");
    var entrega=new EntregaEventoDTO();entrega.setIdDonacionAsociada(1);entrega.setSeguimientoUrl("http://localhost:8083/api/rutas/1/seguimiento");evento.setEntregaAsociada(entrega);
    when(broker.obtenerEventosNoPublicados()).thenReturn(List.of(evento));
    when(servicio.cambiarEstado(eq(1L),any())).thenThrow(new IllegalStateException("fallo")).thenReturn(new DonacionDTO());
    var scheduler=new EventosLogisticaScheduler(broker,servicio);
    scheduler.consumirEventos();verify(broker,never()).marcarPublicado(any());
    scheduler.consumirEventos();verify(broker).marcarPublicado(evento.getIdEvento());
 }
}
