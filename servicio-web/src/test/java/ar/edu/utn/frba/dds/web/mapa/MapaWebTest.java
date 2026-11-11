package ar.edu.utn.frba.dds.web.mapa;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.utn.frba.dds.web.publico.MapaPublico;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

class MapaWebTest {

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    class SinCoordenadas {

        @Autowired
        MockMvc mvc;

        @Test
        void sinCoordenadasElMapaSeMuestraVacioConAvisoYCargaLeaflet() throws Exception {
            mvc.perform(get("/mapa"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(allOf(
                            containsString("las ubicaciones se están incorporando al sistema"),
                            containsString("/webjars/leaflet/1.9.4/dist/leaflet.js"),
                            containsString("/js/mapa.js"),
                            containsString("data-tiles=\"https://tile.openstreetmap.org/{z}/{x}/{y}.png\""),
                            containsString("role=\"region\""),
                            containsString("Ver como lista (<span class=\"mapa__cantidad\">0</span>)"),
                            not(containsString("datos de ejemplo")))));
        }
    }

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @ActiveProfiles("dev")
    class PerfilDev {

        @Autowired
        MockMvc mvc;

        @Test
        void enDesarrolloMuestraDatosDeEjemploAvisandoQueLoSon() throws Exception {
            mvc.perform(get("/mapa"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(allOf(
                            containsString("Estás viendo datos de ejemplo"),
                            containsString("Comedor Escobar Sonrisas"),
                            containsString("Actualizado: 03/11/2026 14:30"))));
        }
    }

    @Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    class ConDatos {

        @Autowired
        MockMvc mvc;

        @MockitoBean
        MapaPublico mapa;

        @Test
        void losMarcadoresVanEscapadosEnUnAtributoYTambienComoLista() throws Exception {
            when(mapa.entregasRealizadas()).thenReturn(new MarcadoresMapa(List.of(
                    Marcador.de(-34.6, -58.4, "</script><script>alert(1)</script>", "Detalle", LocalDateTime.of(2026, 11, 3, 17, 30), Marcador.ENTREGA)),
                    MarcadoresMapa.Origen.REAL));

            mvc.perform(get("/mapa"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(allOf(
                            containsString("data-marcadores=\"[{&quot;lat&quot;:-34.6"),
                            containsString("&quot;titulo&quot;:&quot;&lt;/script&gt;&lt;script&gt;alert(1)&lt;/script&gt;&quot;"),
                            not(containsString("<script>alert(1)")),
                            containsString("&lt;/script&gt;&lt;script&gt;alert(1)&lt;/script&gt;"),
                            not(containsString("las ubicaciones se están incorporando")))));
        }

        @Test
        void elEndpointJsonDevuelveElMismoContrato() throws Exception {
            when(mapa.entregasRealizadas()).thenReturn(new MarcadoresMapa(List.of(
                    Marcador.de(-34.6, -58.4, "Hogar", "Frazadas", LocalDateTime.of(2026, 11, 3, 17, 30), Marcador.CAMION)),
                    MarcadoresMapa.Origen.REAL));

            mvc.perform(get("/mapa/entregas.json"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].lat").value(-34.6))
                    .andExpect(jsonPath("$[0].titulo").value("Hogar"))
                    .andExpect(jsonPath("$[0].actualizadoEn").value("2026-11-03T17:30Z"))
                    .andExpect(jsonPath("$[0].tipo").value("camion"));
        }
    }
}
