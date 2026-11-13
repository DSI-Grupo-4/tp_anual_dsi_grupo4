package ar.edu.utn.frba.dds.incentivos.controller;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.utn.frba.dds.incentivos.exception.GlobalExceptionHandler;
import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Reportes del admin (Persona 4): ranking del mes en curso, con el nombre del donante. */
class RankingEnCursoTest {

    private final MockMvc mvc = MockMvcBuilders
            .standaloneSetup(new IncentivosController(), new RankingController())
            .setControllerAdvice(new GlobalExceptionHandler()).build();

    @Test
    void incluyeAQuienCompletoUnaMisionEsteMesConSuNombre() throws Exception {
        long id = 740101L;
        mvc.perform(put("/api/donantes/" + id + "/perfil").contentType("application/json")
                        .content("{\"nombre\":\"Rita Ranking\",\"medioContactoPreferido\":\"EMAIL\",\"contactoPreferido\":\"rita@example.org\"}"))
                .andExpect(status().isNoContent());
        // Primera misión de Colaborador: racha de 1 mes, se completa con una donación.
        mvc.perform(post("/api/donantes/" + id + "/actividad-donacion").contentType("application/json")
                        .content("{\"donacionId\":940101,\"fecha\":\"" + LocalDate.now() + "\",\"categoriaNombre\":\"ALIMENTOS\","
                                + "\"cantidadBienes\":1,\"donacionExitosa\":false,\"donanteNombre\":\"Rita Ranking\"}"))
                .andExpect(status().isOk());

        mvc.perform(get("/api/ranking/actual"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.donanteId == " + id + ")].donanteNombre", contains("Rita Ranking")))
                .andExpect(jsonPath("$[?(@.donanteId == " + id + ")].cantidadMisiones", contains(1)))
                .andExpect(jsonPath("$[*].mes", everyItem(org.hamcrest.Matchers.is(YearMonth.now().toString()))))
                .andExpect(jsonPath("$[0].puesto").value(1))
                .andExpect(jsonPath("$[*].cantidadMisiones", everyItem(greaterThanOrEqualTo(1))));
    }
}
