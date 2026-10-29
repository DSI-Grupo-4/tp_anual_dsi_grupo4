package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.GestorDonaciones;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoOrganizacion;
import ar.edu.utn.frba.dds.donaciones.dto.EntidadBeneficiariaDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PersonaJuridicaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;

/**
 * Entrega 5 (revisión de reglas de negocio): DELETE /api/entidades/{id} es
 * baja lógica -- la entidad nunca se borra físicamente (conserva historial
 * y relaciones), y no se bloquea aunque tenga donaciones en curso: esas
 * entregas deben poder completarse igual.
 */
@ExtendWith(MockitoExtension.class)
class EntidadBeneficiariaServiceTest {

    @Mock
    private GestorDonaciones gestorDonaciones;

    private EntidadBeneficiariaService entidadBeneficiariaService;

    @BeforeEach
    void setUp() {
        ar.edu.utn.frba.dds.donaciones.repository.EntidadBeneficiariaRepository repo =
                mock(ar.edu.utn.frba.dds.donaciones.repository.EntidadBeneficiariaRepository.class);
        List<EntidadBeneficiaria> guardadas = new java.util.ArrayList<>();
        java.util.concurrent.atomic.AtomicLong siguienteId = new java.util.concurrent.atomic.AtomicLong(1L);
        lenient().when(repo.save(any())).thenAnswer(inv -> {
            EntidadBeneficiaria e = inv.getArgument(0);
            if (e.getId() == null) e.setId(siguienteId.getAndIncrement());
            guardadas.removeIf(x -> x.getId().equals(e.getId()));
            guardadas.add(e);
            return e;
        });
        lenient().when(repo.findAll()).thenAnswer(inv -> List.copyOf(guardadas));
        lenient().when(repo.findById(any())).thenAnswer(inv -> {
            Long id = inv.getArgument(0);
            return guardadas.stream().filter(x -> x.getId().equals(id)).findFirst();
        });
        lenient().doAnswer(inv -> {
            Long id = inv.getArgument(0);
            guardadas.removeIf(x -> x.getId().equals(id));
            return null;
        }).when(repo).deleteById(any());

        entidadBeneficiariaService = new EntidadBeneficiariaService(repo, gestorDonaciones,
                mock(ar.edu.utn.frba.dds.donaciones.repository.DonacionRepository.class));
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
    void eliminarEsBajaLogicaYNoBorraLaEntidad() {
        EntidadBeneficiariaDTO entidad = crearEntidad(null, "Comedor Sonrisas");

        entidadBeneficiariaService.eliminar(entidad.getId());

        // Sigue existiendo (y visible por obtenerTodas()/obtenerPorId()),
        // solo queda inactiva -- nunca se borra físicamente.
        assertThat(entidadBeneficiariaService.obtenerTodas()).hasSize(1);
        assertThat(entidadBeneficiariaService.buscarEntidad(entidad.getId()).isActiva()).isFalse();
    }

    // No debe bloquearse aunque la entidad tenga donaciones en curso
    // asignadas: esas entregas deben poder completarse igual (el vínculo
    // entidad-donación no se toca).
    @Test
    void eliminarNoSeBloqueaAunqueHayaDonacionesEnCurso() {
        EntidadBeneficiariaDTO entidad = crearEntidad(null, "Comedor Sonrisas");

        assertThatCode(() -> entidadBeneficiariaService.eliminar(entidad.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    void reactivarVuelveAHabilitarLaEntidad() {
        EntidadBeneficiariaDTO entidad = crearEntidad(null, "Comedor Sonrisas");
        entidadBeneficiariaService.eliminar(entidad.getId());

        entidadBeneficiariaService.reactivar(entidad.getId());

        assertThat(entidadBeneficiariaService.buscarEntidad(entidad.getId()).isActiva()).isTrue();
    }

    // Única operación de modificación permitida sobre una entidad inactiva
    // es reactivar(): actualizar() debe rechazarla.
    @Test
    void actualizarRechazaSiLaEntidadEstaInactiva() {
        EntidadBeneficiariaDTO entidad = crearEntidad(null, "Comedor Sonrisas");
        entidadBeneficiariaService.eliminar(entidad.getId());

        assertThatThrownBy(() -> entidadBeneficiariaService.actualizar(entidad.getId(), entidad))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("inactiva");
    }

    @Test
    void obtenerEntidadesDominioExcluyeLasInactivas() {
        EntidadBeneficiariaDTO activa = crearEntidad(null, "Escuela Rural 10");
        EntidadBeneficiariaDTO inactiva = crearEntidad(null, "Comedor Sonrisas");
        entidadBeneficiariaService.eliminar(inactiva.getId());

        assertThat(entidadBeneficiariaService.obtenerEntidadesDominio())
                .extracting(e -> e.getId())
                .containsExactly(activa.getId());
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
