package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Deposito;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.GestorDonaciones;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.ItemDonado;
import ar.edu.utn.frba.dds.donaciones.domain.personas.Donante;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.domain.personas.GestorDonantes;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaJuridica;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoOrganizacion;
import ar.edu.utn.frba.dds.donaciones.client.IncentivosClient;
import ar.edu.utn.frba.dds.donaciones.dto.CambioEstadoDTO;
import ar.edu.utn.frba.dds.donaciones.dto.CargaDonacionDTO;
import ar.edu.utn.frba.dds.donaciones.dto.DonacionDTO;
import ar.edu.utn.frba.dds.donaciones.dto.ItemDonadoDTO;
import ar.edu.utn.frba.dds.donaciones.integracion.PublicadorEventosPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ar.edu.utn.frba.dds.donaciones.DatosPrueba;
import java.math.BigDecimal;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests de aplicación con Mockito: GestorDonaciones se mockea, no se toca
 * ninguna base de datos ni ningún otro servicio (todavía no hay persistencia).
 */
@ExtendWith(MockitoExtension.class)
class DonacionServiceTest {

    @Mock
    private GestorDonaciones gestorDonaciones;
    @Mock
    private GestorDonantes gestorDonantes;
    @Mock
    private PublicadorEventosPort publicadorEventos;
    @Mock
    private IncentivosClient incentivosClient;

    private DonacionService donacionService;

    @BeforeEach
    void setUp() {
        donacionService = new DonacionService(gestorDonaciones, gestorDonantes, publicadorEventos, incentivosClient);
    }

    @Test
    void crearSegmentaLaCargaEnUnaDonacionPorItem() {
        when(gestorDonantes.buscarPorId(1L)).thenReturn(new Donante(1L, null));
        when(gestorDonaciones.registrarDonacion(any(Donacion.class)))
                .thenAnswer(invocation -> {
                    Donacion donacion = invocation.getArgument(0);
                    if (donacion.getId() == null) {
                        donacion.setId(1L);
                    }
                    return donacion;
                });

        CargaDonacionDTO carga = new CargaDonacionDTO();
        carga.setDonanteId(1L);
        carga.setDescripcion("Mudanza oficina");

        ItemDonadoDTO sillas = new ItemDonadoDTO();
        sillas.setDescripcion("Sillas de oficina");
        sillas.setSubcategoria(DatosPrueba.subcategoria("sillas"));
        DatosPrueba.completar(sillas);
        sillas.setCantidad(BigDecimal.valueOf(6));

        ItemDonadoDTO mesa = new ItemDonadoDTO();
        mesa.setDescripcion("Mesa rectangular");
        mesa.setSubcategoria(DatosPrueba.subcategoria("mesas"));
        DatosPrueba.completar(mesa);
        mesa.setCantidad(BigDecimal.valueOf(1));

        carga.setItems(List.of(sillas, mesa));

        List<DonacionDTO> resultado = donacionService.crear(carga);

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(DonacionDTO::getDescripcionItem)
                .containsExactly("Sillas de oficina", "Mesa rectangular");
        verify(gestorDonaciones, times(2)).registrarDonacion(any(Donacion.class));
    }

    @Test
    void actualizarMutaLaDonacionExistenteSinTocarSuHistorial() {
        ItemDonado item = DatosPrueba.item(1L, "Frazadas", DatosPrueba.subcategoria("frazadas"), 10, null);
        Donacion existente = DatosPrueba.donacion(1L, item, 10);
        when(gestorDonaciones.buscarPorId(1L)).thenReturn(existente);

        ar.edu.utn.frba.dds.donaciones.dto.ActualizarDonacionDTO cambios = new ar.edu.utn.frba.dds.donaciones.dto.ActualizarDonacionDTO();
        cambios.setItems(List.of(DatosPrueba.dto("Frazadas de invierno", Subcategoria.FRAZADA, 20)));
        when(gestorDonaciones.getDeposito()).thenReturn(new Deposito());

        DonacionDTO actualizado = donacionService.actualizar(1L, cambios);

        assertThat(actualizado.getDescripcionItem()).isEqualTo("Frazadas de invierno");
        assertThat(actualizado.getCantidadAsignada()).isEqualTo(BigDecimal.valueOf(20));
        // Regresión del bug de PUT: no debe perder el historial de estados.
        assertThat(existente.getHistorialEstados()).hasSize(1);
    }

    @Test
    void eliminarDelegaEnGestorDonaciones() {
        donacionService.eliminar(5L);

        verify(gestorDonaciones).eliminar(eq(5L));
    }

    @Test
    void cambiarEstadoPublicaElEventoParaElFuturoConsumoDeNotificaciones() {
        ItemDonado item = DatosPrueba.item(1L, "Frazadas", DatosPrueba.subcategoria("frazadas"), 10, null);
        Donacion existente = DatosPrueba.donacion(1L, item, 10);
        when(gestorDonaciones.buscarPorId(1L)).thenReturn(existente);

        CambioEstadoDTO dto = new CambioEstadoDTO();
        dto.setNuevoEstado(EstadoTrack.ASIGNACION_REALIZADA);

        donacionService.cambiarEstado(1L, dto);

        verify(publicadorEventos).publicar(eq("CAMBIO_ESTADO_DONACION"), any());
        verify(incentivosClient, never()).registrarActividadDonacion(any());
    }

    @Test
    void cambiarEstadoAEntregadaNotificaLaActividadDeDonacionAIncentivos() {
        ItemDonado item = DatosPrueba.item(1L, "Frazadas", DatosPrueba.subcategoria("frazadas"), 10, null);
        Donacion existente = DatosPrueba.donacion(1L, item, 10);
        existente.cambiarEstado(EstadoTrack.ASIGNACION_REALIZADA, null);
        existente.cambiarEstado(EstadoTrack.LISTA_PARA_ENTREGAR, null);
        existente.cambiarEstado(EstadoTrack.EN_TRASLADO, null);
        when(gestorDonaciones.buscarPorId(1L)).thenReturn(existente);

        CambioEstadoDTO dto = new CambioEstadoDTO();
        dto.setNuevoEstado(EstadoTrack.ENTREGADA);

        donacionService.cambiarEstado(1L, dto);

        verify(incentivosClient).registrarActividadDonacion(existente);
    }

    @Test
    void confirmarAsignacionCambiaEstadoAsignaEntidadYPublicaElEvento() {
        // Regresión del hallazgo alto: antes esto lo hacía MatchmakingService
        // mutando el dominio directo, sin pasar por acá — el evento nunca
        // se disparaba para este camino, a diferencia de cambiarEstado().
        ItemDonado item = DatosPrueba.item(1L, "Frazadas", DatosPrueba.subcategoria("frazadas"), 10, null);
        Donacion existente = DatosPrueba.donacion(1L, item, 10);
        when(gestorDonaciones.buscarPorId(1L)).thenReturn(existente);

        EntidadBeneficiaria entidad = new EntidadBeneficiaria(1L,
                new PersonaJuridica("Escuela Rural 10", TipoOrganizacion.GUBERNAMENTAL, null, null),
                "Escuela");

        DonacionDTO resultado = donacionService.confirmarAsignacion(1L, entidad);

        assertThat(resultado.getEstadoActual()).isEqualTo(EstadoTrack.ASIGNACION_REALIZADA);
        assertThat(resultado.getEntidadBeneficiariaId()).isEqualTo(1L);
        assertThat(existente.getEntidadBeneficiaria()).isEqualTo(entidad);
        verify(publicadorEventos).publicar(eq("CAMBIO_ESTADO_DONACION"), any());
    }
}
