package ar.edu.utn.frba.dds.web.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.utn.frba.dds.web.SesionDePrueba;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.EvolucionMes;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.MetricasSistema;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.PosicionRanking;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.PuestoRanking;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.RankingMensual;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosClient;
import ar.edu.utn.frba.dds.web.controller.admin.ReportesAdminController;
import ar.edu.utn.frba.dds.web.error.BackendNoDisponibleException;
import ar.edu.utn.frba.dds.web.error.ServicioBackend;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ReportesAdminWebTest {

    @Autowired MockMvc mvc;
    @MockitoBean IncentivosClient incentivos;

    @BeforeEach
    void datos() {
        when(incentivos.metricasSistema()).thenReturn(new MetricasSistema(12, 40, 9, 9,
                List.of(new EvolucionMes("2026-10", 15, BigDecimal.TEN), new EvolucionMes("2026-11", 25, BigDecimal.ONE))));
        when(incentivos.rankingEnCurso()).thenReturn(List.of(
                new PosicionRanking(1, 7L, "Ana Pérez", 3, "2026-11"),
                new PosicionRanking(2, 8L, null, 1, "2026-11")));
        when(incentivos.historialRankings()).thenReturn(List.of(
                new RankingMensual(LocalDate.of(2026, 10, 1), List.of(new PuestoRanking(9L, "Luis Gómez", 2, 1))),
                new RankingMensual(LocalDate.of(2026, 11, 1), List.of(
                        new PuestoRanking(8L, "Bruno Díaz", 1, 2), new PuestoRanking(7L, "Ana Pérez", 4, 1)))));
    }

    @Test
    void muestraEstadisticasRankingEnCursoEHistorial() throws Exception {
        String html = mvc.perform(get("/admin/reportes").with(SesionDePrueba.conRol("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Sección en construcción"))))
                .andExpect(content().string(containsString("Donantes activos")))
                .andExpect(content().string(containsString("Ver como tabla")))
                .andExpect(content().string(containsString("3 misiones")))
                .andExpect(content().string(containsString("1 misión")))
                // Sin nombre en Incentivos se muestra el ID.
                .andExpect(content().string(containsString("Donante #8")))
                .andExpect(content().string(containsString("Luis Gómez")))
                .andExpect(content().string(containsString("Cerrado el 01/11/2026")))
                // El menú del admin marca Reportes.
                .andExpect(content().string(containsString("href=\"/admin/reportes\"")))
                .andReturn().getResponse().getContentAsString();
        // El historial va del más reciente al más viejo.
        String historial = html.substring(html.indexOf("Historial de rankings"));
        assertThat(historial.indexOf("Bruno Díaz")).isLessThan(historial.indexOf("Luis Gómez"));
        // Y dentro de cada mes, por puesto.
        assertThat(historial.indexOf("Ana Pérez")).isLessThan(historial.indexOf("Bruno Díaz"));
    }

    @Test
    void sinRankingsCerradosNiActividadMuestraEstadosVacios() throws Exception {
        when(incentivos.rankingEnCurso()).thenReturn(List.of());
        when(incentivos.historialRankings()).thenReturn(List.of());
        mvc.perform(get("/admin/reportes").with(SesionDePrueba.conRol("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Todavía nadie completó misiones este mes.")))
                .andExpect(content().string(containsString("Todavía no se cerró ningún ranking mensual.")));
    }

    @Test
    void elRankingEnCursoMuestraLosPrimerosDiez() throws Exception {
        List<PosicionRanking> muchos = new ArrayList<>();
        for (int i = 1; i <= 14; i++) {
            muchos.add(new PosicionRanking(i, (long) i, "Donante " + i, 15 - i, "2026-11"));
        }
        when(incentivos.rankingEnCurso()).thenReturn(muchos);
        mvc.perform(get("/admin/reportes").with(SesionDePrueba.conRol("ADMIN")))
                .andExpect(content().string(containsString("Se muestran los primeros 10 de 14 donantes.")))
                .andExpect(content().string(not(containsString("Donante 11<"))));
    }

    @Test
    void siIncentivosNoRespondeMuestraUnAviso() throws Exception {
        when(incentivos.metricasSistema()).thenThrow(new BackendNoDisponibleException(ServicioBackend.INCENTIVOS, 503, null));
        mvc.perform(get("/admin/reportes").with(SesionDePrueba.conRol("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ahora no pudimos consultar Incentivos")));
    }

    @Test
    void elMesDeCadaRankingEsElAnteriorASuCierre() {
        var historial = ReportesAdminController.historial(List.of(
                new RankingMensual(LocalDate.of(2026, 11, 1), List.of()),
                new RankingMensual(null, List.of())));
        assertThat(historial).hasSize(1);
        assertThat(historial.get(0).mes()).isEqualTo(LocalDate.of(2026, 10, 1));
    }
}
