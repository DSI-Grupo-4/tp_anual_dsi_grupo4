package ar.edu.utn.frba.dds.web.donante;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.utn.frba.dds.web.SesionDePrueba;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Perfil;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteClient;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.EvolucionMes;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.InsigniaObtenida;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.Metricas;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosApi.Mision;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosClient;
import ar.edu.utn.frba.dds.web.error.BackendNoDisponibleException;
import ar.edu.utn.frba.dds.web.error.RecursoNoEncontradoException;
import ar.edu.utn.frba.dds.web.error.ServicioBackend;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "donatrack.web.donante-provisorio-id=1")
@AutoConfigureMockMvc
class IncentivosDonanteWebTest {

    @Autowired MockMvc mvc;
    @MockitoBean PortalDonanteClient donaciones;
    @MockitoBean IncentivosClient incentivos;

    @BeforeEach
    void datos() {
        when(donaciones.perfil(1)).thenReturn(new Perfil(1L, true, "HUMANA", "Ana", "Pérez", null));
        when(incentivos.metricas(1, "HISTORICO")).thenReturn(metricas("HISTORICO", 7));
        when(incentivos.misiones(1)).thenReturn(List.of(
                new Mision("Racha Colaborador", "Colaborador", "Primer Paso", BigDecimal.ONE, BigDecimal.ZERO,
                        true, false, BigDecimal.ONE, "Doná en un mes"),
                new Mision("Confiable Colaborador", "Colaborador", "Mano Amiga", BigDecimal.ONE, BigDecimal.ONE,
                        false, true, BigDecimal.valueOf(2), "Lográ que 2 donaciones lleguen a destino"),
                new Mision("Generoso Colaborador", "Colaborador", "Aporte Destacado", BigDecimal.ZERO, BigDecimal.valueOf(5),
                        false, false, BigDecimal.valueOf(5), "Doná 5 bienes en total")));
        when(incentivos.insigniasObtenidas(1)).thenReturn(List.of(
                new InsigniaObtenida("Primer Paso", null, LocalDate.of(2026, 11, 12), true),
                new InsigniaObtenida("<b>Rara</b>", null, null, false)));
    }

    private static Metricas metricas(String periodo, int donaciones) {
        return new Metricas("Ana Pérez", "Colaborador", periodo, donaciones, 2, 1, 2, BigDecimal.valueOf(4.25), 1,
                List.of(new EvolucionMes("2026-10", 3, BigDecimal.ONE), new EvolucionMes("2026-11", 4, BigDecimal.TEN)));
    }

    @Test
    void muestraCategoriaMisionesEInsignias() throws Exception {
        mvc.perform(get("/donante/incentivos").with(SesionDePrueba.conRol("DONANTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Sección en construcción"))))
                .andExpect(content().string(containsString("Progreso hacia Sostenedor")))
                .andExpect(content().string(containsString("1 de 3 misiones")))
                .andExpect(content().string(containsString("Puesto 1 en el ranking del mes")))
                .andExpect(content().string(containsString("Lográ que 2 donaciones lleguen a destino")))
                .andExpect(content().string(containsString("En curso")))
                .andExpect(content().string(containsString("1 de 2")))
                .andExpect(content().string(containsString("Insignias (2)")))
                .andExpect(content().string(containsString("Obtenida el")))
                .andExpect(content().string(containsString("Oculta")))
                .andExpect(content().string(containsString("Ver como tabla")))
                // Por defecto se abre la pestaña de misiones.
                .andExpect(content().string(containsString("aria-labelledby=\"tab-insignias\" hidden")))
                .andExpect(content().string(not(containsString("aria-labelledby=\"tab-misiones\" hidden"))))
                .andExpect(content().string(not(containsString("<b>Rara</b>"))));
    }

    @Test
    void cambiaDePeriodoYDescartaPeriodosInvalidos() throws Exception {
        when(incentivos.metricas(1, "MENSUAL")).thenReturn(metricas("MENSUAL", 4));
        mvc.perform(get("/donante/incentivos").with(SesionDePrueba.conRol("DONANTE")).param("periodo", "MENSUAL"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tu actividad · Este mes")))
                .andExpect(content().string(containsString("aria-current=\"page\">Este mes</a>")));
        mvc.perform(get("/donante/incentivos").with(SesionDePrueba.conRol("DONANTE")).param("periodo", "SIEMPRE"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tu actividad · Histórico")));
    }

    @Test
    void laPestanaDeInsigniasSePuedeAbrirDesdeLaUrl() throws Exception {
        mvc.perform(get("/donante/incentivos").with(SesionDePrueba.conRol("DONANTE")).param("pestana", "insignias"))
                .andExpect(content().string(containsString("aria-labelledby=\"tab-misiones\" hidden")))
                .andExpect(content().string(not(containsString("aria-labelledby=\"tab-insignias\" hidden"))));
    }

    @Test
    void cambiarLaVisibilidadVuelveALasInsigniasConUnMensaje() throws Exception {
        mvc.perform(post("/donante/incentivos/insignias/visibilidad").with(SesionDePrueba.conRol("DONANTE")).with(csrf()).param("insignia", "Primer Paso").param("visible", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/donante/incentivos?pestana=insignias"));
        verify(incentivos).cambiarVisibilidad(1, "Primer Paso", false);
    }

    @Test
    void siFallaElCambioDeVisibilidadAvisaSinRomper() throws Exception {
        doThrow(new RecursoNoEncontradoException(ServicioBackend.INCENTIVOS, "El donante no tiene la insignia"))
                .when(incentivos).cambiarVisibilidad(1, "Inventada", true);
        mvc.perform(post("/donante/incentivos/insignias/visibilidad").with(SesionDePrueba.conRol("DONANTE")).with(csrf()).param("insignia", "Inventada").param("visible", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/donante/incentivos?pestana=insignias"));
    }

    @Test
    void unaPersonaJuridicaNoPuedeEntrarNiCambiarInsignias() throws Exception {
        when(donaciones.perfil(1)).thenReturn(new Perfil(1L, true, "JURIDICA", null, null, "Panadería El Sol"));
        mvc.perform(get("/donante/incentivos").with(SesionDePrueba.conRol("DONANTE"))).andExpect(redirectedUrl("/donante"));
        mvc.perform(post("/donante/incentivos/insignias/visibilidad").with(SesionDePrueba.conRol("DONANTE")).with(csrf()).param("insignia", "Primer Paso").param("visible", "false"))
                .andExpect(redirectedUrl("/donante"));
        verify(incentivos, never()).metricas(anyLong(), anyString());
        verify(incentivos, never()).cambiarVisibilidad(anyLong(), anyString(), anyBoolean());
    }

    @Test
    void siIncentivosNoRespondeMuestraUnAviso() throws Exception {
        when(incentivos.metricas(1, "HISTORICO")).thenThrow(new BackendNoDisponibleException(ServicioBackend.INCENTIVOS, 503, null));
        mvc.perform(get("/donante/incentivos").with(SesionDePrueba.conRol("DONANTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ahora no pudimos consultar tus incentivos")));
    }
}
