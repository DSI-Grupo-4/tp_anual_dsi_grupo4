package ar.edu.utn.frba.dds.donaciones.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.donaciones.dto.DonacionDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PaginaDTO;
import ar.edu.utn.frba.dds.donaciones.service.ConsultaAdministracionService;
import ar.edu.utn.frba.dds.donaciones.service.EntidadBeneficiariaService;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

/**
 * El endpoint consulta siempre con el id de la ruta como entidad (nunca sin filtro), y recorre todas las páginas.
 * Que solo lo pueda llamar esa entidad o un ADMIN lo decide SeguridadApiConfig (se prueba con Keycloak, no acá).
 */
class DonacionesDeLaEntidadControllerTest {

    private final ConsultaAdministracionService consultas = mock(ConsultaAdministracionService.class);
    private final EntidadBeneficiariaService entidades = mock(EntidadBeneficiariaService.class);
    private final DonacionesDeLaEntidadController controller = new DonacionesDeLaEntidadController(consultas, entidades);

    private static DonacionDTO donacion(long id, long entidad) {
        DonacionDTO dto = new DonacionDTO();
        dto.setId(id);
        dto.setEntidadBeneficiariaId(entidad);
        return dto;
    }

    @Test
    void devuelveLasDeEsaEntidadRecorriendoTodasLasPaginas() {
        when(consultas.donaciones(eq(""), isNull(), isNull(), isNull(), isNull(), eq(7L), eq(0), eq(100)))
                .thenReturn(new PaginaDTO<>(List.of(donacion(3, 7), donacion(2, 7)), 3, 2, 0));
        when(consultas.donaciones(eq(""), isNull(), isNull(), isNull(), isNull(), eq(7L), eq(1), eq(100)))
                .thenReturn(new PaginaDTO<>(List.of(donacion(1, 7)), 3, 2, 1));

        List<DonacionDTO> todas = controller.listar(7L);

        assertThat(todas).extracting(DonacionDTO::getId).containsExactly(3L, 2L, 1L);
        assertThat(todas).allMatch(d -> d.getEntidadBeneficiariaId() == 7L);
    }

    @Test
    void unaEntidadSinDonacionesDevuelveUnaListaVacia() {
        when(consultas.donaciones(eq(""), isNull(), isNull(), isNull(), isNull(), eq(7L), eq(0), eq(100)))
                .thenReturn(new PaginaDTO<>(List.of(), 0, 0, 0));

        assertThat(controller.listar(7L)).isEmpty();
    }

    @Test
    void unaEntidadInexistenteEsUn404SinConsultarDonaciones() {
        when(entidades.buscarEntidad(99L)).thenThrow(new NoSuchElementException("No existe la entidad beneficiaria 99"));

        assertThatThrownBy(() -> controller.listar(99L)).isInstanceOf(NoSuchElementException.class);
        verify(consultas, never()).donaciones(any(), any(), any(), any(), any(), any(), any(Integer.class), any(Integer.class));
    }
}
