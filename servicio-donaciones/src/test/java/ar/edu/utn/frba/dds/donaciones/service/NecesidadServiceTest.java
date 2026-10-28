package ar.edu.utn.frba.dds.donaciones.service;

import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Periodicidad;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.dto.NecesidadDTO;
import ar.edu.utn.frba.dds.donaciones.dto.NecesidadRecurrenteDTO;
import ar.edu.utn.frba.dds.donaciones.repository.NecesidadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ar.edu.utn.frba.dds.donaciones.DatosPrueba;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.UnidadMedida;
import java.math.BigDecimal;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;
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
        NecesidadRepository repo = mock(NecesidadRepository.class);
        List<Necesidad> guardadas = new ArrayList<>();
        AtomicLong siguienteId = new AtomicLong(1L);
        lenient().when(repo.save(any())).thenAnswer(inv -> {
            Necesidad n = inv.getArgument(0);
            if (n.getId() == null) {
                try {
                    var idField = Necesidad.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(n, siguienteId.getAndIncrement());
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            }
            guardadas.removeIf(x -> x.getId().equals(n.getId()));
            guardadas.add(n);
            return n;
        });
        lenient().when(repo.findById(any())).thenAnswer(inv -> {
            Long id = inv.getArgument(0);
            return guardadas.stream().filter(x -> x.getId().equals(id)).findFirst();
        });
        lenient().doAnswer(inv -> {
            Long id = inv.getArgument(0);
            guardadas.removeIf(x -> x.getId().equals(id));
            return null;
        }).when(repo).deleteById(any());

        necesidadService = new NecesidadService(repo, entidadBeneficiariaService);
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
