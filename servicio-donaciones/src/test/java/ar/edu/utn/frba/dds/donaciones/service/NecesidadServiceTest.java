package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Periodicidad;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.dto.NecesidadDTO;
import ar.edu.utn.frba.dds.donaciones.dto.NecesidadRecurrenteDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ar.edu.utn.frba.dds.donaciones.DatosPrueba;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.UnidadMedida;
import java.math.BigDecimal;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Regresión del hallazgo crítico "IDOR en necesidades anidadas": antes,
 * DELETE/PUT /api/entidades/{entidadId}/necesidades/{necesidadId} operaba
 * igual aunque entidadId no coincidiera con la entidad real dueña de esa
 * necesidad (confirmado en vivo con un entidadId inexistente).
 */
@ExtendWith(MockitoExtension.class)
class NecesidadServiceTest {

    @Mock
    private EntidadBeneficiariaService entidadBeneficiariaService;

    private NecesidadService necesidadService;

    @BeforeEach
    void setUp() {
        necesidadService = new NecesidadService(entidadBeneficiariaService);
    }

    private NecesidadDTO crearNecesidadParaEntidad(Long entidadId) {
        EntidadBeneficiaria entidad = new EntidadBeneficiaria(entidadId, null, "Comedor Sonrisas");
        when(entidadBeneficiariaService.buscarEntidad(entidadId)).thenReturn(entidad);

        NecesidadRecurrenteDTO dto = new NecesidadRecurrenteDTO();
        dto.setDescripcion("Fideos semanales");
        dto.setSubcategoria(DatosPrueba.subcategoria("fideos secos"));
        dto.setUnidadMedida(UnidadMedida.UNIDAD);
        dto.setCantidadRequerida(BigDecimal.valueOf(100));
        dto.setPeriodicidad(Periodicidad.SEMANAL);
        dto.setEntidadBeneficiariaId(entidadId);

        return necesidadService.crearRecurrente(dto);
    }

    @Test
    void eliminarRechazaUnEntidadIdQueNoEsElDueñoDeLaNecesidad() {
        NecesidadDTO necesidad = crearNecesidadParaEntidad(1L);

        assertThatThrownBy(() -> necesidadService.eliminar(999L, necesidad.getId()))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void eliminarFuncionaConElEntidadIdCorrecto() {
        NecesidadDTO necesidad = crearNecesidadParaEntidad(1L);

        assertThatCode(() -> necesidadService.eliminar(1L, necesidad.getId()))
                .doesNotThrowAnyException();
    }

    @Test
    void actualizarRecurrenteRechazaUnEntidadIdQueNoEsElDueño() {
        NecesidadDTO necesidad = crearNecesidadParaEntidad(1L);
        NecesidadRecurrenteDTO cambios = new NecesidadRecurrenteDTO();
        cambios.setDescripcion("Cambiado");
        cambios.setSubcategoria(DatosPrueba.subcategoria("fideos secos"));
        cambios.setUnidadMedida(UnidadMedida.UNIDAD);
        cambios.setCantidadRequerida(BigDecimal.valueOf(50));
        cambios.setPeriodicidad(Periodicidad.MENSUAL);

        assertThatThrownBy(() -> necesidadService.actualizarRecurrente(999L, necesidad.getId(), cambios))
                .isInstanceOf(NoSuchElementException.class);
    }
}
