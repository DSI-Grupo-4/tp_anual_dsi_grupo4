package ar.edu.utn.frba.dds.donaciones.integration;

import ar.edu.utn.frba.dds.donaciones.client.LogisticaClient;
import ar.edu.utn.frba.dds.donaciones.dto.DonacionPendienteDTO;
import ar.edu.utn.frba.dds.donaciones.dto.EventoLogisticoDTO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LogisticaBrokerTest {

    private final LogisticaClient propia = mock(LogisticaClient.class);
    private final LogisticaClient alternativa = mock(LogisticaClient.class);
    private final List<DonacionPendienteDTO> lote = List.of(new DonacionPendienteDTO());

    @Test
    void alternaLosProveedoresEnRoundRobin() {
        LogisticaBroker broker = brokerRoundRobin();

        broker.enviarLote(lote);
        broker.enviarLote(lote);

        verify(propia).enviarLote(lote);
        verify(alternativa).enviarLote(lote);
    }

    @Test
    void usaElSiguienteProveedorCuandoElSeleccionadoFalla() {
        LogisticaBroker broker = brokerRoundRobin();
        doThrow(new RuntimeException("proveedor caido")).when(propia).enviarLote(lote);

        broker.enviarLote(lote);

        verify(propia).enviarLote(lote);
        verify(alternativa).enviarLote(lote);
    }

    @Test
    void fallaCuandoNingunProveedorPuedeRecibirElLote() {
        LogisticaBroker broker = brokerRoundRobin();
        doThrow(new RuntimeException("propia caida")).when(propia).enviarLote(lote);
        doThrow(new RuntimeException("alternativa caida")).when(alternativa).enviarLote(lote);

        assertThrows(IllegalStateException.class, () -> broker.enviarLote(lote));
    }

    @Test
    void consultaEventosDeTodosLosProveedores() {
        LogisticaBroker broker = brokerRoundRobin();
        EventoLogisticoDTO eventoPropio = evento();
        EventoLogisticoDTO eventoAlternativo = evento();
        when(propia.obtenerEventosNoPublicados()).thenReturn(List.of(eventoPropio));
        when(alternativa.obtenerEventosNoPublicados()).thenReturn(List.of(eventoAlternativo));

        List<EventoLogisticoDTO> eventos = broker.obtenerEventosNoPublicados();

        assertEquals(List.of(eventoPropio, eventoAlternativo), eventos);
        verify(propia).obtenerEventosNoPublicados();
        verify(alternativa).obtenerEventosNoPublicados();
    }

    @Test
    void continuaConsultandoSiUnProveedorFalla() {
        LogisticaBroker broker = brokerRoundRobin();
        EventoLogisticoDTO eventoAlternativo = evento();
        when(propia.obtenerEventosNoPublicados()).thenThrow(new RuntimeException("propia caida"));
        when(alternativa.obtenerEventosNoPublicados()).thenReturn(List.of(eventoAlternativo));

        List<EventoLogisticoDTO> eventos = broker.obtenerEventosNoPublicados();

        assertEquals(List.of(eventoAlternativo), eventos);
        verify(alternativa).obtenerEventosNoPublicados();
    }

    @Test
    void confirmaElEventoEnElProveedorQueLoGenero() {
        LogisticaBroker broker = brokerRoundRobin();
        EventoLogisticoDTO evento = evento();
        when(alternativa.obtenerEventosNoPublicados()).thenReturn(List.of(evento));
        when(propia.obtenerEventosNoPublicados()).thenReturn(List.of());

        broker.obtenerEventosNoPublicados();
        broker.marcarPublicado(evento.getIdEvento());

        verify(alternativa).marcarPublicado(evento.getIdEvento());
        verify(propia, never()).marcarPublicado(evento.getIdEvento());
    }

    private LogisticaBroker brokerRoundRobin() {
        return new LogisticaBroker(
                List.of(propia, alternativa),
                LogisticaBroker.EstrategiaSeleccion.ROUND_ROBIN);
    }

    private EventoLogisticoDTO evento() {
        EventoLogisticoDTO evento = new EventoLogisticoDTO();
        evento.setIdEvento(UUID.randomUUID());
        return evento;
    }
}
