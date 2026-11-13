package ar.edu.utn.frba.dds.web.beneficiaria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ar.edu.utn.frba.dds.web.client.donaciones.DonacionApi;
import ar.edu.utn.frba.dds.web.client.donaciones.DonacionesClient;
import ar.edu.utn.frba.dds.web.client.donaciones.NecesidadApi;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class PortalBeneficiariaServiceTest {

    private final DonacionesClient donaciones = mock(DonacionesClient.class);
    private final PortalBeneficiariaService portal = new PortalBeneficiariaService(donaciones);

    private static DonacionApi donacion(long id, String estado, Long entidad) {
        return new DonacionApi(id, "Item " + id, BigDecimal.TEN, "UNIDAD", "ALIMENTOS", "ARROZ", estado, entidad);
    }

    @Test
    void soloDevuelveLasDonacionesDeLaEntidadDeLaMasNuevaALaMasVieja() {
        when(donaciones.donacionesDeEntidad(7L)).thenReturn(List.of(
                donacion(1, "ENTREGADA", 7L), donacion(2, "EN_TRASLADO", 8L), donacion(3, "EN_TRASLADO", 7L)));

        List<DonacionApi> propias = portal.donacionesDe(7L);

        assertThat(propias).extracting(DonacionApi::id).containsExactly(3L, 1L);
    }

    @Test
    void elResumenDistingueLasEnProcesoDeLasRecibidas() {
        List<DonacionApi> propias = List.of(
                donacion(1, "ENTREGADA", 7L), donacion(2, "ASIGNACION_REALIZADA", 7L),
                donacion(3, "LISTA_PARA_ENTREGAR", 7L), donacion(4, "EN_TRASLADO", 7L),
                donacion(5, "ENTREGA_FALLIDA", 7L));

        ResumenDonaciones resumen = portal.resumenDonaciones(propias);

        assertThat(resumen).isEqualTo(new ResumenDonaciones(5, 3, 1));
    }

    @Test
    void lasUrgentesSonLasExtraordinariasSinCubrir() {
        List<NecesidadApi> necesidades = List.of(
                new NecesidadApi(1L, "RECURRENTE", "Leche", "ARROZ", "KILOGRAMO", BigDecimal.TEN, BigDecimal.ZERO, false, "MENSUAL", null),
                new NecesidadApi(2L, "EXTRAORDINARIA", "Frazadas", "FRAZADA", "UNIDAD", BigDecimal.TEN, BigDecimal.ZERO, false, null, "SISMO"),
                new NecesidadApi(3L, "EXTRAORDINARIA", "Sillas", "SILLA", "UNIDAD", BigDecimal.TEN, BigDecimal.TEN, true, null, "SISMO"));

        ResumenNecesidades resumen = portal.resumenNecesidades(necesidades);

        assertThat(resumen).isEqualTo(new ResumenNecesidades(3, 2, 1, 1));
    }
}
