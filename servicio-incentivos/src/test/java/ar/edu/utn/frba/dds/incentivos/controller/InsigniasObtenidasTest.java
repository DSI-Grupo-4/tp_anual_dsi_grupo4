package ar.edu.utn.frba.dds.incentivos.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.utn.frba.dds.incentivos.exception.GlobalExceptionHandler;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Portal del donante (Persona 4): insignias ocultas y objetivo de cada misión. */
class InsigniasObtenidasTest {

    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new IncentivosController())
            .setControllerAdvice(new GlobalExceptionHandler()).build();

    private void crearDonante(long id) throws Exception {
        mvc.perform(put("/api/donantes/" + id + "/perfil").contentType("application/json")
                        .content("{\"nombre\":\"Ana Perez\",\"medioContactoPreferido\":\"EMAIL\",\"contactoPreferido\":\"ana@example.org\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void unaInsigniaOcultaSigueEnObtenidasYSePuedeVolverAMostrar() throws Exception {
        long id = 740001L;
        crearDonante(id);
        // La primera misión de Colaborador es una racha de 1 mes: una donación la completa.
        mvc.perform(post("/api/donantes/" + id + "/actividad-donacion").contentType("application/json")
                        .content("{\"donacionId\":940001,\"fecha\":\"" + LocalDate.now() + "\",\"categoriaNombre\":\"ALIMENTOS\","
                                + "\"cantidadBienes\":2,\"donacionExitosa\":false,\"donanteNombre\":\"Ana Perez\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.insigniaNombre").value("Primer Paso"));

        mvc.perform(patch("/api/donantes/" + id + "/insignias/Primer Paso/visibilidad").contentType("application/json")
                        .content("{\"visible\":false}"))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/donantes/" + id + "/insignias")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/donantes/" + id + "/insignias/obtenidas")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].insigniaNombre").value("Primer Paso"))
                .andExpect(jsonPath("$[0].visible").value(false));

        mvc.perform(patch("/api/donantes/" + id + "/insignias/Primer Paso/visibilidad").contentType("application/json")
                        .content("{\"visible\":true}"))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/donantes/" + id + "/insignias/obtenidas"))
                .andExpect(jsonPath("$[0].visible").value(true));
    }

    @Test
    void cadaMisionInformaSuMetaYQueHayQueHacer() throws Exception {
        long id = 740002L;
        crearDonante(id);
        mvc.perform(get("/api/donantes/" + id + "/misiones")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].meta").value(1))
                .andExpect(jsonPath("$[0].objetivo").value("Doná en un mes"))
                .andExpect(jsonPath("$[1].meta").value(2))
                .andExpect(jsonPath("$[1].objetivo").value("Lográ que 2 donaciones lleguen a destino"));
    }

    @Test
    void obtenidasDeUnDonanteSinActividadEsUnaListaVacia() throws Exception {
        long id = 740003L;
        crearDonante(id);
        mvc.perform(get("/api/donantes/" + id + "/insignias/obtenidas")).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
