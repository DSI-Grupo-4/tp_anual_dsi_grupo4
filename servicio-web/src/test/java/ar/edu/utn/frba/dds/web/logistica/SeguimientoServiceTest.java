package ar.edu.utn.frba.dds.web.logistica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.web.client.logistica.CamionApi;
import ar.edu.utn.frba.dds.web.client.logistica.EntregaAdminApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.ChoferApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.DireccionApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.EntregaDeRutaApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.ParadaApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutasClient;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private static EnvioEntidad envio(int ruta, int parada, int total, String estado, EntregaDeRutaApi entrega) {
        return new EnvioEntidad(ruta, parada, parada, total, LocalDate.of(2026, 11, 12), estado, "AA123BB",
                null, null, null, false, List.of(entrega));
    }

    @Test
    void losEnviosDeLaEntidadMuestranPrimeroLoQueEstaEnCamino() {
        when(rutasClient.enviosPropios(5L)).thenReturn(List.of(
                envio(1, 1, 1, "FINALIZADA", entrega(1, "ENTREGADA")),
                envio(2, 1, 1, "PLANIFICADA", entrega(2, "ASIGNADA_A_RUTA")),
                envio(3, 1, 1, "INICIADA", entrega(3, "EN_TRASLADO"))));

        assertThat(seguimiento.enviosDe(5L)).extracting(EnvioEntidad::idRuta).containsExactly(3, 2, 1);
    }

    @Test
    void elResumenDeLaFlotaAgrupaAsignadosYEnRutaComoEnUso() {
        ResumenFlota resumen = seguimiento.resumenFlota(List.of(
                new CamionApi(1, "A", 1, 1, 1, "DISPONIBLE"), new CamionApi(2, "B", 1, 1, 1, "ASIGNADO"),
                new CamionApi(3, "C", 1, 1, 1, "EN_RUTA"), new CamionApi(4, "D", 1, 1, 1, "FUERA_DE_SERVICIO")));

        assertThat(resumen).isEqualTo(new ResumenFlota(4, 1, 2, 1));
    }

    @Test
    void losEnviosDeLaEntidadLlevanLaUbicacionDelCamionSiLaInformo() {
        when(rutasClient.enviosPropios(5L)).thenReturn(List.of(
                new EnvioEntidad(1, 1, 1, 1, LocalDate.of(2026, 11, 12), "INICIADA", "AA123BB",
                        -34.61, -58.39, LocalDateTime.of(2026, 11, 12, 17, 30), true, List.of(entrega(1, "EN_TRASLADO"))),
                envio(2, 1, 1, "PLANIFICADA", entrega(2, "ASIGNADA_A_RUTA"))));

        List<EnvioEntidad> envios = seguimiento.enviosDe(5L);

        EnvioEntidad enCamino = envios.get(0);
        assertThat(enCamino.tieneUbicacion()).isTrue();
        assertThat(enCamino.latitud()).isEqualTo(-34.61);
        assertThat(enCamino.ubicacionSimulada()).isTrue();
        assertThat(envios.get(1).tieneUbicacion()).isFalse();
    }

    @Test
    void elEnvioDeUnaRutaSoloSeEncuentraSiLeLlevaAlgoALaEntidad() {
        when(rutasClient.enviosPropios(5L)).thenReturn(List.of(
                envio(2, 1, 1, "INICIADA", entrega(1, "EN_TRASLADO"))));

        assertThat(seguimiento.envioDe(5L, 2)).isPresent();
        assertThat(seguimiento.envioDe(5L, 3)).isEmpty();
        assertThat(seguimiento.envioDe(5L, 99)).isEmpty();
    }

    @Test
    void sePuedeConfirmarSoloConLaRutaEnCursoYEntregasEnTraslado() {
        when(rutasClient.enviosPropios(5L)).thenReturn(List.of(
                envio(1, 1, 1, "INICIADA", entrega(1, "EN_TRASLADO")),
                envio(2, 1, 1, "INICIADA", entrega(2, "ENTREGADA")),
                envio(3, 1, 1, "PLANIFICADA", entrega(3, "ASIGNADA_A_RUTA"))));

        assertThat(seguimiento.envioDe(5L, 1).orElseThrow().puedeConfirmarRecepcion()).isTrue();
        assertThat(seguimiento.envioDe(5L, 2).orElseThrow().puedeConfirmarRecepcion()).isFalse();
        assertThat(seguimiento.envioDe(5L, 3).orElseThrow().puedeConfirmarRecepcion()).isFalse();
    }

    @Test
    void elResumenDeEntregasAgrupaLosEstadosEnCuatroCifras() {
        List<EntregaAdminApi> entregas = List.of(
                new EntregaAdminApi(1, 1, 5, "PENDIENTE", null, null, null),
                new EntregaAdminApi(2, 2, 5, "REPLANIFICABLE", null, null, null),
                new EntregaAdminApi(3, 3, 5, "ASIGNADA_A_RUTA", null, null, null),
                new EntregaAdminApi(4, 4, 5, "EN_TRASLADO", null, null, null),
                new EntregaAdminApi(5, 5, 5, "ENTREGADA", null, null, null),
                new EntregaAdminApi(6, 6, 5, "NO_RECIBIDA", null, null, "Nadie atendió"),
                new EntregaAdminApi(7, 7, 5, "FALLIDA", null, null, "Calle cortada"));

        assertThat(seguimiento.resumenEntregas(entregas)).isEqualTo(new ResumenEntregas(2, 2, 1, 2));
    }
}
