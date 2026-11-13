package ar.edu.utn.frba.dds.donaciones.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.UnidadMedida;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.GestorDonaciones;
import ar.edu.utn.frba.dds.donaciones.domain.lugares.Ciudad;
import ar.edu.utn.frba.dds.donaciones.domain.lugares.Direccion;
import ar.edu.utn.frba.dds.donaciones.domain.lugares.Provincia;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.NecesidadRecurrente;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Periodicidad;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaHumana;
import ar.edu.utn.frba.dds.donaciones.domain.personas.PersonaJuridica;
import ar.edu.utn.frba.dds.donaciones.domain.personas.TipoOrganizacion;
import ar.edu.utn.frba.dds.donaciones.dto.FichaPublicaEntidadDTO;
import ar.edu.utn.frba.dds.donaciones.repository.DonacionRepository;
import ar.edu.utn.frba.dds.donaciones.repository.EntidadBeneficiariaRepository;
import ar.edu.utn.frba.dds.donaciones.repository.NecesidadRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

class CatalogoEntidadesTest {

    @Test
    void laFichaNoIncluyeCalleNiRepresentantesYElListadoOmiteInactivas() throws Exception {
        EntidadBeneficiaria activa = entidad(1L, "Comedor Norte", "Calle Secreta 123");
        EntidadBeneficiaria inactiva = entidad(2L, "Comedor Sur", "Otra Calle");
        inactiva.desactivar();
        EntidadBeneficiariaRepository repositorio = mock(EntidadBeneficiariaRepository.class);
        when(repositorio.findAll()).thenReturn(List.of(activa, inactiva));
        when(repositorio.findById(1L)).thenReturn(java.util.Optional.of(activa));
        EntidadBeneficiariaService servicio = new EntidadBeneficiariaService(
                repositorio, mock(GestorDonaciones.class), mock(DonacionRepository.class));

        assertThat(servicio.fichasActivas()).extracting(FichaPublicaEntidadDTO::id).containsExactly(1L);
        String json = new ObjectMapper().writeValueAsString(servicio.fichaPublica(1L));
        assertThat(json).contains("Comedor Norte").doesNotContain("Calle Secreta", "representantes", "mediosContacto");
    }

    @Test
    void lasNecesidadesDelCatalogoSonSoloLasAbiertasDeUnaEntidadActiva() {
        EntidadBeneficiaria activa = entidad(1L, "Comedor Norte", "Calle Secreta");
        NecesidadRecurrente abierta = necesidad(10L, BigDecimal.TEN);
        NecesidadRecurrente cubierta = necesidad(11L, BigDecimal.TEN);
        cubierta.recibir(BigDecimal.TEN);
        NecesidadRepository necesidades = mock(NecesidadRepository.class);
        EntidadBeneficiariaRepository entidades = mock(EntidadBeneficiariaRepository.class);
        when(entidades.findById(1L)).thenReturn(java.util.Optional.of(activa));
        when(necesidades.findByEntidadBeneficiaria_Id(1L)).thenReturn(List.of(abierta, cubierta));
        EntidadBeneficiariaService entidadesServicio = new EntidadBeneficiariaService(
                entidades, mock(GestorDonaciones.class), mock(DonacionRepository.class));
        NecesidadService servicio = new NecesidadService(necesidades, entidadesServicio, mock(DonacionRepository.class));

        assertThat(servicio.obtenerAbiertas(1L)).extracting(dto -> dto.getId()).containsExactly(10L);

        EntidadBeneficiaria inactiva = entidad(2L, "Cerrada", "Otra");
        inactiva.desactivar();
        when(entidades.findById(2L)).thenReturn(java.util.Optional.of(inactiva));
        assertThatThrownBy(() -> servicio.obtenerAbiertas(2L)).isInstanceOf(NoSuchElementException.class);
    }

    private static EntidadBeneficiaria entidad(Long id, String nombre, String calle) {
        EntidadBeneficiaria entidad = new EntidadBeneficiaria(id,
                new PersonaJuridica(nombre, TipoOrganizacion.ONG, "Alimentos", List.of(mock(PersonaHumana.class))),
                "Comedor");
        entidad.setDireccion(new Direccion(calle, "100", new Ciudad("Córdoba", new Provincia("Córdoba"))));
        return entidad;
    }

    private static NecesidadRecurrente necesidad(Long id, BigDecimal cantidad) {
        return new NecesidadRecurrente(id, "Arroz", Subcategoria.ARROZ, UnidadMedida.KILOGRAMO, cantidad, Periodicidad.MENSUAL);
    }
}
