package ar.edu.utn.frba.dds.web.logistica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.utn.frba.dds.web.SesionDePrueba;
import ar.edu.utn.frba.dds.web.client.donaciones.DonacionesClient;
import ar.edu.utn.frba.dds.web.client.logistica.CamionApi;
import ar.edu.utn.frba.dds.web.client.logistica.FotoSubidaApi;
import ar.edu.utn.frba.dds.web.client.logistica.FotosClient;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.ChoferApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.CiudadApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.DireccionApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.EntregaDeRutaApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutaApi.ParadaApi;
import ar.edu.utn.frba.dds.web.client.logistica.RutasClient;
import ar.edu.utn.frba.dds.web.error.BackendRechazoException;
import ar.edu.utn.frba.dds.web.error.ServicioBackend;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Confirmación de recepción con fotos de la entidad (la entidad de las pruebas es la 1), con Logística simulado. */
@SpringBootTest
@AutoConfigureMockMvc
class RecepcionWebTest {

    private static final byte[] BYTES = {1, 2, 3, 4};

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RutasClient rutas;

    @MockitoBean
    private FotosClient fotos;

    @MockitoBean
    private DonacionesClient donaciones;

    private static EntregaDeRutaApi entrega(int id, String estado) {
        return new EntregaDeRutaApi(id, id + 100, estado, null, null, List.of());
    }

    private static ParadaApi parada(int numero, int entidad, EntregaDeRutaApi... entregas) {
        return new ParadaApi(numero, numero, entidad, new DireccionApi("Calle Ajena", "1", new CiudadApi("Escobar")), List.of(entregas));
    }

    private static RutaApi ruta(int id, String estado, ParadaApi... paradas) {
        return new RutaApi(id, LocalDate.of(2026, 11, 12), estado, new CamionApi(1, "AA123BB", 20, 3, 1500, "EN_RUTA"),
                new ChoferApi(1, "Juan Pérez"), List.of(paradas));
    }

    private static MockMultipartFile foto(String nombre, String tipo) {
        return new MockMultipartFile("fotos", nombre, tipo, BYTES);
    }

    @BeforeEach
    void datosBase() {
        // Ruta 2: una parada de otra entidad y la parada 2, que es de la entidad 1 y tiene una entrega en traslado.
        when(rutas.rutas()).thenReturn(List.of(
                ruta(2, "INICIADA", parada(1, 9, entrega(1, "EN_TRASLADO")), parada(2, 1, entrega(2, "EN_TRASLADO"))),
                ruta(3, "INICIADA", parada(1, 9, entrega(3, "EN_TRASLADO")))));
    }

    @Test
    void elFormularioMuestraLoQueSeConfirmaYElCampoDeFotosSinDatosAjenos() throws Exception {
        mvc.perform(get("/beneficiaria/seguimiento/2").with(SesionDePrueba.conRol("BENEFICIARIA")))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("Confirmar recepción"),
                        containsString("Donación 102"),
                        containsString("type=\"file\""),
                        containsString("multiple"),
                        containsString("enctype=\"multipart/form-data\""),
                        not(containsString("Donación 101")),
                        not(containsString("Calle Ajena")),
                        not(containsString("Juan Pérez")))));
    }

    @Test
    void unaRutaQueNoLeLlevaNadaALaEntidadEsUn404() throws Exception {
        mvc.perform(get("/beneficiaria/seguimiento/3").with(SesionDePrueba.conRol("BENEFICIARIA"))).andExpect(status().isNotFound());
        mvc.perform(get("/beneficiaria/seguimiento/99").with(SesionDePrueba.conRol("BENEFICIARIA"))).andExpect(status().isNotFound());
    }

    @Test
    void siYaNoHayEntregasEnTrasladoNoSeOfreceElFormulario() throws Exception {
        when(rutas.rutas()).thenReturn(List.of(ruta(2, "INICIADA", parada(1, 1, entrega(2, "ENTREGADA")))));

        mvc.perform(get("/beneficiaria/seguimiento/2").with(SesionDePrueba.conRol("BENEFICIARIA")))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("No hay entregas en traslado para confirmar"),
                        not(containsString("type=\"file\"")))));
    }

    @Test
    void confirmarSubeLasFotosYUsaLaParadaDeLaEntidadAunqueIntentenOtra() throws Exception {
        when(fotos.subir(eq("a.jpg"), eq("image/jpeg"), any())).thenReturn(new FotoSubidaApi("1", "http://l/api/fotos/1"));
        when(fotos.subir(eq("b.png"), eq("image/png"), any())).thenReturn(new FotoSubidaApi("2", "http://l/api/fotos/2"));

        mvc.perform(multipart("/beneficiaria/seguimiento/2/recepcion")
                        .file(foto("a.jpg", "image/jpeg")).file(foto("b.png", "image/png"))
                        .param("idParada", "1") // un dato que mandan a mano: se ignora
                        .with(SesionDePrueba.conRol("BENEFICIARIA")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/beneficiaria/seguimiento"))
                .andExpect(flash().attributeExists("toasts"));

        verify(rutas).confirmarRecepcion(2, 2, List.of("http://l/api/fotos/1", "http://l/api/fotos/2"));
    }

    @Test
    void unCampoDeArchivoVacioComoElQueEnviaElNavegadorSinElegirNadaCuentaComoSinFotos() throws Exception {
        mvc.perform(multipart("/beneficiaria/seguimiento/2/recepcion")
                        .file(new MockMultipartFile("fotos", "", "application/octet-stream", new byte[0]))
                        .with(SesionDePrueba.conRol("BENEFICIARIA")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Elegí al menos una foto")));

        verifyNoInteractions(fotos);
        verify(rutas, never()).confirmarRecepcion(any(), any(), anyList());
    }

    @Test
    void masDeCincoFotosSeRechazan() throws Exception {
        var pedido = multipart("/beneficiaria/seguimiento/2/recepcion");
        for (int i = 0; i < 6; i++) {
            pedido.file(foto("f" + i + ".jpg", "image/jpeg"));
        }

        mvc.perform(pedido.with(SesionDePrueba.conRol("BENEFICIARIA")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("hasta 5 fotos")));

        verifyNoInteractions(fotos);
    }

    @Test
    void unArchivoQueNoEsImagenSeRechazaAntesDeSubirNada() throws Exception {
        mvc.perform(multipart("/beneficiaria/seguimiento/2/recepcion")
                        .file(foto("a.jpg", "image/jpeg")).file(foto("virus.html", "text/html"))
                        .with(SesionDePrueba.conRol("BENEFICIARIA")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(containsString("virus.html"), containsString("no es una imagen JPEG, PNG o WebP"))));

        verifyNoInteractions(fotos);
    }

    @Test
    void unaFotoDeMasDeCincoMegasSeRechaza() throws Exception {
        byte[] grande = new byte[5 * 1024 * 1024 + 1];

        mvc.perform(multipart("/beneficiaria/seguimiento/2/recepcion")
                        .file(new MockMultipartFile("fotos", "grande.jpg", "image/jpeg", grande))
                        .with(SesionDePrueba.conRol("BENEFICIARIA")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("pesa más de 5 MB")));

        verifyNoInteractions(fotos);
    }

    @Test
    void siLogisticaRechazaUnaFotoSeMuestraSuMensajeYNoSeConfirma() throws Exception {
        when(fotos.subir(anyString(), anyString(), any())).thenThrow(new BackendRechazoException(
                ServicioBackend.LOGISTICA, 400, "El contenido del archivo no es una imagen JPEG, PNG o WebP válida", Map.of()));

        mvc.perform(multipart("/beneficiaria/seguimiento/2/recepcion")
                        .file(foto("a.jpg", "image/jpeg"))
                        .with(SesionDePrueba.conRol("BENEFICIARIA")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("El contenido del archivo no es una imagen")));

        verify(rutas, never()).confirmarRecepcion(any(), any(), anyList());
    }

    @Test
    void siLaConfirmacionSeRechazaSeExplicaEnElFormulario() throws Exception {
        when(fotos.subir(anyString(), anyString(), any())).thenReturn(new FotoSubidaApi("1", "http://l/api/fotos/1"));
        org.mockito.Mockito.doThrow(new BackendRechazoException(ServicioBackend.LOGISTICA, 409,
                        "La parada 2 no tiene entregas en traslado para confirmar", Map.of()))
                .when(rutas).confirmarRecepcion(eq(2), eq(2), anyList());

        mvc.perform(multipart("/beneficiaria/seguimiento/2/recepcion")
                        .file(foto("a.jpg", "image/jpeg"))
                        .with(SesionDePrueba.conRol("BENEFICIARIA")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("no tiene entregas en traslado")));
    }

    @Test
    void noSePuedeConfirmarLaRutaDeOtraEntidadNiSubirFotos() throws Exception {
        mvc.perform(multipart("/beneficiaria/seguimiento/3/recepcion")
                        .file(foto("a.jpg", "image/jpeg"))
                        .with(SesionDePrueba.conRol("BENEFICIARIA")).with(csrf()))
                .andExpect(status().isNotFound());

        verifyNoInteractions(fotos);
        verify(rutas, never()).confirmarRecepcion(any(), any(), anyList());
    }

    @Test
    void siLaRutaYaNoTieneEntregasParaConfirmarVuelveAlSeguimientoConUnAviso() throws Exception {
        when(rutas.rutas()).thenReturn(List.of(ruta(2, "FINALIZADA", parada(1, 1, entrega(2, "ENTREGADA")))));

        mvc.perform(multipart("/beneficiaria/seguimiento/2/recepcion")
                        .file(foto("a.jpg", "image/jpeg"))
                        .with(SesionDePrueba.conRol("BENEFICIARIA")).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/beneficiaria/seguimiento"))
                .andExpect(flash().attributeExists("toasts"));

        verifyNoInteractions(fotos);
    }

    @Test
    void elSeguimientoOfreceConfirmarSoloEnLasRutasConEntregasEnTraslado() throws Exception {
        when(rutas.rutas()).thenReturn(List.of(
                ruta(2, "INICIADA", parada(1, 1, entrega(2, "EN_TRASLADO"))),
                ruta(4, "FINALIZADA", parada(1, 1, entrega(4, "ENTREGADA")))));

        mvc.perform(get("/beneficiaria/seguimiento").with(SesionDePrueba.conRol("BENEFICIARIA")))
                .andExpect(status().isOk())
                .andExpect(content().string(allOf(
                        containsString("href=\"/beneficiaria/seguimiento/2\""),
                        not(containsString("href=\"/beneficiaria/seguimiento/4\"")))));
    }

    @Test
    void lasUrlsEnviadasAChequeadasLlevanElOrdenDeLasFotos() throws Exception {
        when(fotos.subir(anyString(), anyString(), any()))
                .thenReturn(new FotoSubidaApi("1", "http://l/1"), new FotoSubidaApi("2", "http://l/2"));

        mvc.perform(multipart("/beneficiaria/seguimiento/2/recepcion")
                .file(foto("a.jpg", "image/jpeg")).file(foto("b.jpg", "image/jpeg"))
                .with(SesionDePrueba.conRol("BENEFICIARIA")).with(csrf()));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> urls = ArgumentCaptor.forClass(List.class);
        verify(rutas).confirmarRecepcion(eq(2), eq(2), urls.capture());
        assertThat(urls.getValue()).containsExactly("http://l/1", "http://l/2");
    }
}
