package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.GestorDonaciones;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.ItemDonado;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoOrganizacion;
import ar.edu.utn.frba.dds.donaciones.dto.EntidadBeneficiariaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaJuridicaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ar.edu.utn.frba.dds.donaciones.DatosPrueba;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Regresión del hallazgo crítico "integridad referencial al eliminar":
 * antes, DELETE /api/entidades/{id} borraba la entidad aunque tuviera una
 * donación activamente asignada, dejando una referencia colgante visible
 * incluso en /api/donaciones/pendientes.
 */
@ExtendWith(MockitoExtension.class)
class EntidadBeneficiariaServiceTest {

    @Mock
    private GestorDonaciones gestorDonaciones;

    private EntidadBeneficiariaService entidadBeneficiariaService;

    @BeforeEach
    void setUp() {
        entidadBeneficiariaService = new EntidadBeneficiariaService(gestorDonaciones);
    }

    private EntidadBeneficiariaDTO crearEntidad(Long id, String razonSocial) {
        EntidadBeneficiariaDTO dto = new EntidadBeneficiariaDTO();
        PersonaJuridicaDTO persona = new PersonaJuridicaDTO();
        var contacto = new ar.edu.utn.frba.dds.donaciones.dto.MedioContactoDTO();
        contacto.setTipo(ar.edu.utn.frba.dds.donaciones.domain.personas.TipoContacto.EMAIL);
        contacto.setValor("entidad@example.org"); contacto.setEsPreferido(true);
        persona.setMediosContacto(java.util.List.of(contacto));
        persona.setRazonSocial(razonSocial);
        persona.setTipo(TipoOrganizacion.ONG);
        dto.setPersonaJuridica(persona);
        dto.setDescripcion(razonSocial);
        return entidadBeneficiariaService.crear(dto);
    }

    @Test
    void rechazaElBorradoSiHayUnaDonacionActivaAsignada() {
        EntidadBeneficiariaDTO entidad = crearEntidad(null, "Comedor Sonrisas");
        EntidadBeneficiaria entidadDominio = entidadBeneficiariaService.buscarEntidad(entidad.getId());

        ItemDonado item = DatosPrueba.item(1L, "Fideos", DatosPrueba.subcategoria("fideos secos"), 100, null);
        Donacion donacionActiva = DatosPrueba.donacion(1L, item, 100);
        donacionActiva.cambiarEstado(EstadoTrack.ASIGNACION_REALIZADA, null);
        donacionActiva.setEntidadBeneficiaria(entidadDominio);

        when(gestorDonaciones.getDonaciones()).thenReturn(List.of(donacionActiva));

        assertThatThrownBy(() -> entidadBeneficiariaService.eliminar(entidad.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("donación");

        assertThat(entidadBeneficiariaService.obtenerTodas()).hasSize(1); // no se borró
    }

    @Test
    void permiteElBorradoSiNoHayDonacionesActivas() {
        EntidadBeneficiariaDTO entidad = crearEntidad(null, "Escuela Rural 10");

        when(gestorDonaciones.getDonaciones()).thenReturn(List.of());

        assertThatCode(() -> entidadBeneficiariaService.eliminar(entidad.getId()))
                .doesNotThrowAnyException();
        assertThat(entidadBeneficiariaService.obtenerTodas()).isEmpty();
    }

    @Test
    void permiteElBorradoSiLasDonacionesDeLaEntidadYaEstanEntregadas() {
        // Estados terminales (ENTREGADA/ENTREGA_FALLIDA/VENCIDA) no bloquean
        // el borrado — solo los "en curso" (ver ESTADOS_ACTIVOS).
        EntidadBeneficiariaDTO entidad = crearEntidad(null, "Comedor Sonrisas");
        EntidadBeneficiaria entidadDominio = entidadBeneficiariaService.buscarEntidad(entidad.getId());

        ItemDonado item = DatosPrueba.item(1L, "Fideos", DatosPrueba.subcategoria("fideos secos"), 100, null);
        Donacion donacionEntregada = DatosPrueba.donacion(1L, item, 100);
        donacionEntregada.cambiarEstado(EstadoTrack.ASIGNACION_REALIZADA, null);
        donacionEntregada.cambiarEstado(EstadoTrack.LISTA_PARA_ENTREGAR, null);
        donacionEntregada.cambiarEstado(EstadoTrack.EN_TRASLADO, null);
        donacionEntregada.cambiarEstado(EstadoTrack.ENTREGADA, null);
        donacionEntregada.setEntidadBeneficiaria(entidadDominio);

        when(gestorDonaciones.getDonaciones()).thenReturn(List.of(donacionEntregada));

        assertThatCode(() -> entidadBeneficiariaService.eliminar(entidad.getId()))
                .doesNotThrowAnyException();
    }
    @Test
    void contactosDeEntidadSeConservanYSePuedenActualizar() {
        var entidad=crearEntidad(null,"Comedor");
        assertThat(entidad.getPersonaJuridica().getMediosContacto()).hasSize(1);
        entidad.getPersonaJuridica().getMediosContacto().get(0).setValor("nuevo@example.org");
        entidadBeneficiariaService.actualizar(entidad.getId(),entidad);
        assertThat(entidadBeneficiariaService.buscarEntidad(entidad.getId()).getEntidad().medioPreferido().orElseThrow().getValor()).isEqualTo("nuevo@example.org");
    }
}
