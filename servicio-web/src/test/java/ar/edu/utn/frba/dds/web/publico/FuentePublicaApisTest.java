package ar.edu.utn.frba.dds.web.publico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.web.client.donaciones.DonacionApi;
import ar.edu.utn.frba.dds.web.client.donaciones.DonacionesClient;
import ar.edu.utn.frba.dds.web.client.donaciones.DonanteApi;
import ar.edu.utn.frba.dds.web.client.donaciones.EntidadApi;
import ar.edu.utn.frba.dds.web.client.logistica.EntregaApi;
import ar.edu.utn.frba.dds.web.client.logistica.LogisticaClient;
import ar.edu.utn.frba.dds.web.error.BackendNoDisponibleException;
import ar.edu.utn.frba.dds.web.error.ServicioBackend;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FuentePublicaApisTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 10, 15, 0);

    private final LogisticaClient logistica = mock(LogisticaClient.class);
    private final DonacionesClient donaciones = mock(DonacionesClient.class);
    private FuentePublicaApis fuente;

    @BeforeEach
    void setUp() {
        Clock reloj = Clock.fixed(AHORA.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        fuente = new FuentePublicaApis(logistica, donaciones, reloj);
        when(donaciones.donaciones()).thenReturn(List.of(
                new DonacionApi(15L, "Arroz 1 kg", new BigDecimal("20.00"), "KILOGRAMO", "ALIMENTOS", "ARROZ", "ENTREGADA", 3L),
                new DonacionApi(16L, null, new BigDecimal("12"), "UNIDAD", "MOBILIARIO", "SILLA", "ENTREGADA", 4L)));
        when(donaciones.entidades()).thenReturn(List.of(
                new EntidadApi(3L, true, null, new EntidadApi.PersonaJuridica("Comedor Escobar Sonrisas")),
                new EntidadApi(4L, true, null, new EntidadApi.PersonaJuridica("Escuela Rural N°10")),
                new EntidadApi(5L, false, null, null)));
        when(donaciones.donantes()).thenReturn(List.of(new DonanteApi(1L, true), new DonanteApi(2L, false), new DonanteApi(3L, null)));
    }

    private static EntregaApi entrega(int id, int donacion, int entidad, String estado, LocalDateTime cuando, String... fotos) {
        List<EntregaApi.FotoApi> lista = new ArrayList<>();
        for (String url : fotos) {
            lista.add(new EntregaApi.FotoApi(url, null));
        }
        return new EntregaApi(id, donacion, entidad, estado, null, cuando, lista);
    }

    @Test
    void lasCifrasCuentanEntregasDelUltimoMesEnTrasladoYActivos() {
        when(logistica.entregas()).thenReturn(List.of(
                entrega(1, 15, 3, "ENTREGADA", AHORA.minusDays(2), "https://f/1.jpg"),
                entrega(2, 16, 4, "ENTREGADA", AHORA.minusDays(40), "https://f/2.jpg"),
                entrega(3, 16, 4, "EN_TRASLADO", null),
                entrega(4, 15, 3, "ENTREGADA", null)));

        CifrasPublicas cifras = fuente.resumen().cifras();

        assertThat(cifras.entregadasUltimoMes()).isEqualTo(1);
        assertThat(cifras.enCamino()).isEqualTo(1);
        assertThat(cifras.entidadesActivas()).isEqualTo(2);
        assertThat(cifras.donantesActivos()).isEqualTo(2);
    }

    @Test
    void lasDestacadasSonDelUltimoMesConFotoPrimeroYConDatosDeDonacionYEntidad() {
        when(logistica.entregas()).thenReturn(List.of(
                entrega(1, 15, 3, "ENTREGADA", AHORA.minusDays(1)),
                entrega(2, 16, 4, "ENTREGADA", AHORA.minusDays(5), "javascript:alert(1)", "https://f/sillas.jpg"),
                entrega(3, 15, 3, "ENTREGADA", AHORA.minusDays(31), "https://f/vieja.jpg")));

        List<DonacionPublica> destacadas = fuente.resumen().destacadas();

        assertThat(destacadas).hasSize(2);
        DonacionPublica conFoto = destacadas.get(0);
        assertThat(conFoto.fotoUrl()).isEqualTo("https://f/sillas.jpg");
        assertThat(conFoto.subcategoria()).isEqualTo("SILLA");
        assertThat(conFoto.cantidad()).isEqualTo("12");
        assertThat(conFoto.entidad()).isEqualTo("Escuela Rural N°10");
        assertThat(destacadas.get(1).descripcion()).isEqualTo("Arroz 1 kg");
        assertThat(destacadas.get(1).cantidad()).isEqualTo("20");
        assertThat(destacadas.get(1).tieneFoto()).isFalse();
    }

    @Test
    void siLogisticaNoRespondeLaLandingSigueConLasCifrasDeDonaciones() {
        when(logistica.entregas()).thenThrow(new BackendNoDisponibleException(ServicioBackend.LOGISTICA, 0, null));

        ResumenPublico resumen = fuente.resumen();

        assertThat(resumen.entregasDisponibles()).isFalse();
        assertThat(resumen.destacadas()).isEmpty();
        assertThat(resumen.cifras().entregadasUltimoMes()).isNull();
        assertThat(resumen.cifras().entidadesActivas()).isEqualTo(2);
    }

    @Test
    void siDonacionesNoRespondeLasDestacadasSalenSinDescripcionNiEntidad() {
        when(logistica.entregas()).thenReturn(List.of(entrega(1, 15, 3, "ENTREGADA", AHORA.minusDays(1), "https://f/1.jpg")));
        when(donaciones.donaciones()).thenThrow(new BackendNoDisponibleException(ServicioBackend.DONACIONES, 0, null));
        when(donaciones.entidades()).thenThrow(new BackendNoDisponibleException(ServicioBackend.DONACIONES, 0, null));

        ResumenPublico resumen = fuente.resumen();

        assertThat(resumen.destacadas()).singleElement().satisfies(d -> {
            assertThat(d.fotoUrl()).isEqualTo("https://f/1.jpg");
            assertThat(d.descripcion()).isNull();
            assertThat(d.entidad()).isNull();
        });
        assertThat(resumen.cifras().entidadesActivas()).isNull();
    }

    @Test
    void laGaleriaMuestraUnaTarjetaPorFotoSeguraOrdenadaYPaginada() {
        when(logistica.entregas()).thenReturn(List.of(
                entrega(1, 15, 3, "ENTREGADA", AHORA.minusDays(10), "https://f/a.jpg", "https://f/b.jpg"),
                entrega(2, 16, 4, "ENTREGADA", AHORA.minusDays(1), "https://f/c.jpg", "data:image/png;base64,xx"),
                entrega(3, 16, 4, "EN_TRASLADO", null, "https://f/no.jpg"),
                entrega(4, 16, 4, "ENTREGADA", AHORA.minusDays(3))));

        PaginaFotos primera = fuente.fotos(1, 2);
        PaginaFotos segunda = fuente.fotos(2, 2);

        assertThat(primera.total()).isEqualTo(3);
        assertThat(primera.totalPaginas()).isEqualTo(2);
        assertThat(primera.elementos()).extracting(DonacionPublica::fotoUrl).containsExactly("https://f/c.jpg", "https://f/a.jpg");
        assertThat(segunda.elementos()).extracting(DonacionPublica::fotoUrl).containsExactly("https://f/b.jpg");
        assertThat(segunda.tieneSiguiente()).isFalse();
        assertThat(fuente.fotos(99, 2).pagina()).isEqualTo(2);
    }

    @Test
    void laGaleriaSinLogisticaPropagaElErrorParaMostrarLaPagina503() {
        when(logistica.entregas()).thenThrow(new BackendNoDisponibleException(ServicioBackend.LOGISTICA, 0, null));

        assertThatThrownBy(() -> fuente.fotos(1, 12)).isInstanceOf(BackendNoDisponibleException.class);
    }

    @Test
    void soloSeAceptanUrlsHttp() {
        assertThat(FuentePublicaApis.esUrlSegura("https://ejemplo.org/a.jpg")).isTrue();
        assertThat(FuentePublicaApis.esUrlSegura(" HTTP://ejemplo.org/a.jpg")).isTrue();
        assertThat(FuentePublicaApis.esUrlSegura("javascript:alert(1)")).isFalse();
        assertThat(FuentePublicaApis.esUrlSegura("file:///etc/passwd")).isFalse();
        assertThat(FuentePublicaApis.esUrlSegura(null)).isFalse();
    }
}
