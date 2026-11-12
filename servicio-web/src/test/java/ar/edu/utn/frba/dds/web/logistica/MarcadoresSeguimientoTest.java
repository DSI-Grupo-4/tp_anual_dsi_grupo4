package ar.edu.utn.frba.dds.web.logistica;

import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.utn.frba.dds.web.client.logistica.CamionApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.ChoferApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.CiudadApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.DireccionApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.EntregaDeRutaApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.ParadaApi;
import ar.edu.utn.frba.dds.web.mapa.Marcador;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

class MarcadoresSeguimientoTest {

    private static final LocalDateTime HORA = LocalDateTime.of(2026, 11, 12, 17, 30);

    private final MarcadoresSeguimiento marcadores = new MarcadoresSeguimiento(mensajes());

    private static ResourceBundleMessageSource mensajes() {
        ResourceBundleMessageSource fuente = new ResourceBundleMessageSource();
        fuente.setBasenames("messages-logistica");
        fuente.setDefaultEncoding("UTF-8");
        fuente.setFallbackToSystemLocale(false);
        LocaleContextHolder.setLocale(Locale.forLanguageTag("es-AR"));
        return fuente;
    }

    private static CamionApi camionEn(Double lat, Double lng, boolean simulada) {
        return new CamionApi(1, "AA123BB", 20, 3, 1500, "EN_RUTA", lat, lng, lat == null ? null : HORA, lat == null ? null : simulada);
    }

    private static EntregaDeRutaApi entrega(int id, String estado) {
        return new EntregaDeRutaApi(id, id + 100, estado, null, null, List.of());
    }

    private static ParadaApi parada(int numero, int entidad, Double lat, Double lng, EntregaDeRutaApi... entregas) {
        return new ParadaApi(numero, numero, entidad,
                new DireccionApi("Calle", String.valueOf(numero), new CiudadApi("Escobar"), lat, lng), List.of(entregas));
    }

    private static RutaApi ruta(int id, String estado, CamionApi camion, ParadaApi... paradas) {
        return new RutaApi(id, LocalDate.of(2026, 11, 12), estado, camion, new ChoferApi(1, "Juan Pérez"), List.of(paradas));
    }

    @Test
    void soloSeDibujanLosCamionesDeRutasEnCursoQueYaInformaronUbicacion() {
        List<RutaApi> rutas = List.of(
                ruta(1, "INICIADA", camionEn(-34.61, -58.39, false), parada(1, 5, null, null, entrega(1, "EN_TRASLADO"))),
                ruta(2, "INICIADA", camionEn(null, null, false), parada(1, 5, null, null, entrega(2, "EN_TRASLADO"))),
                ruta(3, "PLANIFICADA", camionEn(-34.7, -58.4, false), parada(1, 5, null, null, entrega(3, "ASIGNADA_A_RUTA"))),
                ruta(4, "FINALIZADA", camionEn(-34.8, -58.5, false), parada(1, 5, null, null, entrega(4, "ENTREGADA"))));

        List<Marcador> lista = marcadores.camionesEnCurso(rutas);

        assertThat(lista).hasSize(1);
        Marcador camion = lista.get(0);
        assertThat(camion.tipo()).isEqualTo(Marcador.CAMION);
        assertThat(camion.titulo()).isEqualTo("Camión AA123BB");
        assertThat(camion.detalle()).isEqualTo("Ruta 1 · 1 entregas en traslado");
        assertThat(camion.lat()).isEqualTo(-34.61);
        assertThat(camion.actualizadoEn()).isEqualTo("2026-11-12T17:30Z");
    }

    @Test
    void unaUbicacionSimuladaSeAvisaEnElDetalle() {
        List<RutaApi> rutas = List.of(
                ruta(1, "INICIADA", camionEn(-34.61, -58.39, true), parada(1, 5, null, null, entrega(1, "EN_TRASLADO"))));

        assertThat(marcadores.camionesEnCurso(rutas).get(0).detalle()).endsWith("Ubicación simulada");
    }

    @Test
    void elDetalleDeUnaRutaDibujaElCamionYSoloLasParadasConCoordenadas() {
        RutaApi ruta = ruta(1, "INICIADA", camionEn(-34.61, -58.39, false),
                parada(1, 5, -34.5, -58.3, entrega(1, "EN_TRASLADO")),
                parada(2, 6, null, null, entrega(2, "EN_TRASLADO")));

        List<Marcador> lista = marcadores.deRuta(ruta, Map.of(5, "Fundación Demo"));

        assertThat(lista).extracting(Marcador::tipo).containsExactly(Marcador.CAMION, Marcador.SEDE);
        assertThat(lista.get(1).titulo()).isEqualTo("Parada 1: Fundación Demo");
        assertThat(lista.get(1).detalle()).isEqualTo("Calle 1, Escobar");
    }

    @Test
    void siFaltaElNombreDeLaEntidadSeUsaSuNumero() {
        RutaApi ruta = ruta(1, "PLANIFICADA", camionEn(null, null, false),
                parada(1, 5, -34.5, -58.3, entrega(1, "ASIGNADA_A_RUTA")));

        List<Marcador> lista = marcadores.deRuta(ruta, Map.of());

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).titulo()).isEqualTo("Parada 1: Entidad 5");
    }

    @Test
    void laEntidadSoloVeElCamionDeLasRutasEnCursoConUbicacionYNadaDeLasOtrasParadas() {
        List<EnvioEntidad> envios = List.of(
                new EnvioEntidad(1, 2, 2, 3, LocalDate.of(2026, 11, 12), "INICIADA", "AA123BB",
                        -34.61, -58.39, HORA, false, List.of(entrega(1, "EN_TRASLADO"))),
                new EnvioEntidad(2, 1, 1, 1, LocalDate.of(2026, 11, 12), "PLANIFICADA", "CC456DD",
                        -34.7, -58.4, HORA, false, List.of(entrega(2, "ASIGNADA_A_RUTA"))),
                new EnvioEntidad(3, 1, 1, 1, LocalDate.of(2026, 11, 12), "INICIADA", "EE789FF",
                        null, null, null, false, List.of(entrega(3, "EN_TRASLADO"))));

        List<Marcador> lista = marcadores.paraEntidad(envios);

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).titulo()).isEqualTo("Camión AA123BB");
        assertThat(lista.get(0).detalle()).isEqualTo("Ruta 1 · parada 2 de 3");
    }

    @Test
    void elDonanteVeSoloLosCamionesQueLlevanSusDonacionesSinContarLasAjenas() {
        List<RutaApi> rutas = List.of(
                ruta(1, "INICIADA", camionEn(-34.61, -58.39, false),
                        parada(1, 5, null, null, entrega(1, "EN_TRASLADO"), entrega(2, "EN_TRASLADO")),
                        parada(2, 6, null, null, entrega(3, "EN_TRASLADO"))),
                ruta(2, "INICIADA", camionEn(-34.7, -58.4, false), parada(1, 5, null, null, entrega(4, "EN_TRASLADO"))),
                ruta(3, "PLANIFICADA", camionEn(-34.8, -58.5, false), parada(1, 5, null, null, entrega(5, "ASIGNADA_A_RUTA"))));

        // las donaciones del donante son la 101 (entrega 1) y la 105 (entrega 5, todavía sin salir)
        List<Marcador> lista = marcadores.paraDonaciones(List.of(101L, 105L), rutas);

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).detalle()).isEqualTo("Lleva 1 de tus donaciones");
    }
}
