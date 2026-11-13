package ar.edu.utn.frba.dds.web.error;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PaginasErrorWebTest.ControllerQueFalla.class)
class PaginasErrorWebTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void siUnBackendNoRespondeSeMuestraLaPagina503ConReintento() throws Exception {
        mvc.perform(get("/prueba/no-disponible"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string(allOf(
                        containsString("Servicio no disponible"),
                        containsString("Servicio afectado: Donaciones"),
                        containsString("Reintentar"),
                        containsString("class=\"saltar-contenido\""))));
    }

    @Test
    void un404DelBackendMuestraLaPagina404() throws Exception {
        mvc.perform(get("/prueba/no-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("No encontramos esta página")));
    }

    @Test
    void unRechazoNoAtrapadoMuestraElMensajeDelBackend() throws Exception {
        mvc.perform(get("/prueba/rechazo"))
                .andExpect(status().isConflict())
                .andExpect(content().string(allOf(
                        containsString("No se pudo completar la operación"),
                        containsString("La donación ya fue asignada"))));
    }

    @Test
    void unErrorInesperadoDelBackendMuestraLaPagina500SinDetallesTecnicos() throws Exception {
        mvc.perform(get("/prueba/error"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(allOf(
                        containsString("Algo salió mal"),
                        org.hamcrest.Matchers.not(containsString("NullPointer")))));
    }

    @Controller
    static class ControllerQueFalla {

        @GetMapping("/prueba/no-disponible")
        String noDisponible() {
            throw new BackendNoDisponibleException(ServicioBackend.DONACIONES, 0, null);
        }

        @GetMapping("/prueba/no-encontrado")
        String noEncontrado() {
            throw new RecursoNoEncontradoException(ServicioBackend.DONACIONES, "No existe");
        }

        @GetMapping("/prueba/rechazo")
        String rechazo() {
            throw new BackendRechazoException(ServicioBackend.DONACIONES, 409, "La donación ya fue asignada", Map.of());
        }

        @GetMapping("/prueba/error")
        String error() {
            throw new BackendException(ServicioBackend.DONACIONES, 500, "NullPointerException en el backend", null);
        }
    }
}
