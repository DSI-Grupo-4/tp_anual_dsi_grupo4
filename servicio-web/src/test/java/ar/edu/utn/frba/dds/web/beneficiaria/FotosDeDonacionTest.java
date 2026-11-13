package ar.edu.utn.frba.dds.web.beneficiaria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.web.client.logistica.EntregaApi.FotoApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.EntregaDeRutaApi;
import ar.edu.utn.frba.dds.web.error.BackendNoDisponibleException;
import ar.edu.utn.frba.dds.web.error.ServicioBackend;
import ar.edu.utn.frba.dds.web.logistica.EnvioEntidad;
import ar.edu.utn.frba.dds.web.logistica.SeguimientoService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Las fotos salen solo de los envíos de la entidad que mira y solo de entregas ya recibidas. */
class FotosDeDonacionTest {

    private final SeguimientoService seguimiento = mock(SeguimientoService.class);
    private final FotosDeDonacion fotos = new FotosDeDonacion(seguimiento);

    private static EnvioEntidad envio(EntregaDeRutaApi... entregas) {
        return new EnvioEntidad(1, 1, 1, 1, LocalDate.of(2026, 11, 12), "FINALIZADA", "AA123BB",
                null, null, null, false, List.of(entregas));
    }

    private static EntregaDeRutaApi entrega(int donacion, String estado, String url) {
        return new EntregaDeRutaApi(donacion, donacion, estado, null, null, List.of(new FotoApi(url, null)));
    }

    @Test
    void devuelveLasFotosDeLaDonacionPedidaConsultandoSoloLosEnviosDeLaEntidad() {
        when(seguimiento.enviosDe(5L)).thenReturn(List.of(envio(
                entrega(10, "ENTREGADA", "https://f/10.jpg"), entrega(11, "ENTREGADA", "https://f/11.jpg"))));

        assertThat(fotos.de(5L, 10L)).extracting(FotoApi::url).containsExactly("https://f/10.jpg");
        verify(seguimiento).enviosDe(5L);
    }

    @Test
    void unaEntregaQueNoSeRecibioNoMuestraFotos() {
        when(seguimiento.enviosDe(5L)).thenReturn(List.of(envio(entrega(10, "EN_TRASLADO", "https://f/10.jpg"))));

        assertThat(fotos.de(5L, 10L)).isEmpty();
    }

    @Test
    void unaDonacionQueNoEsDeLaEntidadNoTieneFotos() {
        when(seguimiento.enviosDe(5L)).thenReturn(List.of());

        assertThat(fotos.de(5L, 99L)).isEmpty();
    }

    @Test
    void siLogisticaNoRespondeDevuelveUnaListaVacia() {
        when(seguimiento.enviosDe(5L)).thenThrow(new BackendNoDisponibleException(ServicioBackend.LOGISTICA, 503, null));

        assertThat(fotos.de(5L, 10L)).isEmpty();
    }
}
