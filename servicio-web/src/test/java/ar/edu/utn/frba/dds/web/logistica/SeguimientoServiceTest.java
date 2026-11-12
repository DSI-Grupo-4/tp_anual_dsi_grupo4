package ar.edu.utn.frba.dds.web.logistica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.web.client.logistica.CamionApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.ChoferApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.DireccionApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.EntregaDeRutaApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.ParadaApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutasClient;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class SeguimientoServiceTest {

    private final RutasClient rutasClient = mock(RutasClient.class);
    private final SeguimientoService seguimiento = new SeguimientoService(rutasClient);

    private static EntregaDeRutaApi entrega(int id, String estado) {
        return new EntregaDeRutaApi(id, id + 100, estado, null, null, List.of());
    }

    private static ParadaApi parada(int numero, int entidad, EntregaDeRutaApi... entregas) {
        return new ParadaApi(numero, numero, entidad, new DireccionApi("Calle", String.valueOf(numero), null), List.of(entregas));
    }

    private static RutaApi ruta(int id, String estado, ParadaApi... paradas) {
        return new RutaApi(id, LocalDate.of(2026, 11, 12), estado, new CamionApi(1, "AA123BB", 20, 3, 1500, "ASIGNADO"),
                new ChoferApi(1, "Juan"), List.of(paradas));
    }

    @Test
    void lasRutasVienenDeLaMasNuevaALaMasVieja() {
        when(rutasClient.rutas()).thenReturn(List.of(ruta(1, "FINALIZADA"), ruta(3, "PLANIFICADA"), ruta(2, "INICIADA")));

        assertThat(seguimiento.rutas(null)).extracting(RutaApi::idRuta).containsExactly(3, 2, 1);
        assertThat(seguimiento.rutas("INICIADA")).extracting(RutaApi::idRuta).containsExactly(2);
    }

    @Test
    void elResumenCuentaLasRutasPorEstadoYLasEntregasSinResultadoIgnorandoLasCanceladas() {
        List<RutaApi> todas = List.of(
                ruta(1, "PLANIFICADA", parada(1, 5, entrega(1, "ASIGNADA_A_RUTA"))),
                ruta(2, "INICIADA", parada(1, 5, entrega(2, "EN_TRASLADO"), entrega(3, "ENTREGADA"))),
                ruta(3, "FINALIZADA", parada(1, 5, entrega(4, "ENTREGADA"))),
                ruta(4, "CANCELADA", parada(1, 5, entrega(5, "ASIGNADA_A_RUTA"))));

        ResumenSeguimiento resumen = seguimiento.resumen(todas);

        assertThat(resumen).isEqualTo(new ResumenSeguimiento(1, 1, 1, 2));
    }

    @Test
    void unaEntidadSoloRecibeSusParadasYNoLasDeLasOtras() {
        when(rutasClient.rutas()).thenReturn(List.of(
                ruta(1, "INICIADA", parada(1, 9, entrega(1, "EN_TRASLADO")), parada(2, 5, entrega(2, "EN_TRASLADO"))),
                ruta(2, "CANCELADA", parada(1, 5, entrega(3, "ASIGNADA_A_RUTA")))));

        List<EnvioEntidad> envios = seguimiento.enviosDe(5L);

        assertThat(envios).hasSize(1);
        EnvioEntidad envio = envios.get(0);
        assertThat(envio.idRuta()).isEqualTo(1);
        assertThat(envio.numeroParada()).isEqualTo(2);
        assertThat(envio.totalParadas()).isEqualTo(2);
        assertThat(envio.patente()).isEqualTo("AA123BB");
        assertThat(envio.entregas()).extracting(EntregaDeRutaApi::idEntrega).containsExactly(2);
    }

    @Test
    void losEnviosDeLaEntidadMuestranPrimeroLoQueEstaEnCamino() {
        when(rutasClient.rutas()).thenReturn(List.of(
                ruta(1, "FINALIZADA", parada(1, 5, entrega(1, "ENTREGADA"))),
                ruta(2, "PLANIFICADA", parada(1, 5, entrega(2, "ASIGNADA_A_RUTA"))),
                ruta(3, "INICIADA", parada(1, 5, entrega(3, "EN_TRASLADO")))));

        assertThat(seguimiento.enviosDe(5L)).extracting(EnvioEntidad::idRuta).containsExactly(3, 2, 1);
    }

    @Test
    void elResumenDeLaFlotaAgrupaAsignadosYEnRutaComoEnUso() {
        ResumenFlota resumen = seguimiento.resumenFlota(List.of(
                new CamionApi(1, "A", 1, 1, 1, "DISPONIBLE"), new CamionApi(2, "B", 1, 1, 1, "ASIGNADO"),
                new CamionApi(3, "C", 1, 1, 1, "EN_RUTA"), new CamionApi(4, "D", 1, 1, 1, "FUERA_DE_SERVICIO")));

        assertThat(resumen).isEqualTo(new ResumenFlota(4, 1, 2, 1));
    }
}
