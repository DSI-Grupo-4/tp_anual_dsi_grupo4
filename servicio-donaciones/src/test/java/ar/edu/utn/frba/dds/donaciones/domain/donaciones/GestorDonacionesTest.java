package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.necesidades.NecesidadRecurrente;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Periodicidad;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaJuridica;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoOrganizacion;
import ar.edu.utn.frba.dds.donaciones.repository.DonacionRepository;
import ar.edu.utn.frba.dds.donaciones.repository.SolicitudDonacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ar.edu.utn.frba.dds.donaciones.DatosPrueba;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.UnidadMedida;
import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// DonacionRepository se simula con un mock respaldado por una lista en
// memoria (en vez de @DataJpaTest contra H2): estos tests verifican el
// comportamiento de GestorDonaciones en sí (delegación, no pisar el id si
// ya vino asignado por otra vía), no el mapeo JPA -- eso ya lo cubre
// IncentivosPersistenciaTest/RutaPersistenciaTest como patrón para
// "DonacionPersistenciaTest" si hiciera falta en el futuro.
class GestorDonacionesTest {

    private GestorDonaciones gestor;
    private final List<Donacion> donacionesGuardadas = new ArrayList<>();
    private final AtomicLong siguienteId = new AtomicLong(1L);

    @BeforeEach
    void setUp() {
        DonacionRepository donacionRepository = mock(DonacionRepository.class);
        SolicitudDonacionRepository solicitudDonacionRepository = mock(SolicitudDonacionRepository.class);

        when(donacionRepository.save(any())).thenAnswer(invocation -> {
            Donacion donacion = invocation.getArgument(0);
            if (donacion.getId() == null) {
                donacion.setId(siguienteId.getAndIncrement());
            }
            donacionesGuardadas.removeIf(d -> d.getId().equals(donacion.getId()));
            donacionesGuardadas.add(donacion);
            return donacion;
        });
        when(donacionRepository.findAll()).thenAnswer(invocation -> List.copyOf(donacionesGuardadas));
        when(donacionRepository.findById(any())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return donacionesGuardadas.stream().filter(d -> d.getId().equals(id)).findFirst();
        });
        doAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            donacionesGuardadas.removeIf(d -> d.getId().equals(id));
            return null;
        }).when(donacionRepository).deleteById(any());

        when(solicitudDonacionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        gestor = new GestorDonaciones(new Deposito(), donacionRepository, solicitudDonacionRepository);
    }

    private Donacion nuevaDonacion(Long id, String subcategoria) {
        return DatosPrueba.donacion(id, DatosPrueba.item(null, subcategoria, DatosPrueba.subcategoria(subcategoria), 1, null), 1);
    }

    @Test
    void registrarDonacionAsignaIdCuandoNoTiene() {
        Donacion registrada = gestor.registrarDonacion(nuevaDonacion(null, "frazadas"));

        assertThat(registrada.getId()).isNotNull();
        assertThat(gestor.getDonaciones()).containsExactly(registrada);
    }

    @Test
    void idsSecuencialesNoColisionanEntreVariasDonaciones() {
        Donacion d1 = gestor.registrarDonacion(nuevaDonacion(null, "a"));
        Donacion d2 = gestor.registrarDonacion(nuevaDonacion(null, "b"));

        assertThat(d1.getId()).isNotEqualTo(d2.getId());
    }

    @Test
    void buscarPorIdLanzaExcepcionSiNoExiste() {
        assertThatThrownBy(() -> gestor.buscarPorId(404L))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void eliminarSacaLaDonacionDeLaLista() {
        Donacion donacion = gestor.registrarDonacion(nuevaDonacion(null, "frazadas"));

        gestor.eliminar(donacion.getId());

        assertThat(gestor.getDonaciones()).isEmpty();
    }

    @Test
    void ejecutarMatchmakingIntersectaAmbosAlgoritmos() {
        Donacion donacion = DatosPrueba.donacion(1L,
                DatosPrueba.item(1L, "Fideos", DatosPrueba.subcategoria("fideos secos"), 100, null), 100);

        EntidadBeneficiaria conNecesidad = new EntidadBeneficiaria(1L,
                new PersonaJuridica("Comedor Sonrisas", TipoOrganizacion.ONG, null, null),
                "Comedor Sonrisas");
        conNecesidad.agregarNecesidad(new NecesidadRecurrente(1L, "Fideos semanales",
                DatosPrueba.subcategoria("fideos secos"), UnidadMedida.UNIDAD, BigDecimal.valueOf(100), Periodicidad.SEMANAL));

        EntidadBeneficiaria sinNecesidad = new EntidadBeneficiaria(2L,
                new PersonaJuridica("Escuela Rural 10", TipoOrganizacion.GUBERNAMENTAL, null, null),
                "Escuela Rural 10");

        ResultadoMatchmaking resultado = gestor.ejecutarMatchmaking(
                donacion, List.of(conNecesidad, sinNecesidad));

        // PrioridadSubatendidos no filtra (ordena todas por menos atendidas),
        // así que la intersección queda determinada por CompatibilidadSemantica.
        assertThat(resultado.getPorCompatibilidad()).containsExactly(conNecesidad);
        assertThat(resultado.getInterseccion()).containsExactly(conNecesidad);
    }
}
