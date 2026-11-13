package ar.edu.utn.frba.dds.web.donante;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Ciudad;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Direccion;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.DonacionDelDonante;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Entidad;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Necesidad;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Pagina;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Perfil;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.PersonaJuridica;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteApi.Provincia;
import ar.edu.utn.frba.dds.web.client.donaciones.PortalDonanteClient;
import ar.edu.utn.frba.dds.web.client.incentivos.IncentivosClient;
import ar.edu.utn.frba.dds.web.error.BackendNoDisponibleException;
import ar.edu.utn.frba.dds.web.error.RecursoNoEncontradoException;
import ar.edu.utn.frba.dds.web.error.ServicioBackend;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
class EntidadesDonanteWebTest {

    @Autowired MockMvc mvc;
    @MockitoBean PortalDonanteClient donaciones;
    @MockitoBean IncentivosClient incentivos;

    private static Entidad entidad(long id, boolean activa, String nombre, String tipo, String ciudad, String provincia) {
        return new Entidad(id, activa, "Descripción de " + nombre, new PersonaJuridica(nombre, tipo, "Alimentos"),
                new Direccion(new Ciudad(ciudad, new Provincia(provincia))));
    }

    @BeforeEach
    void datos() {
        when(donaciones.perfil(1)).thenReturn(new Perfil(1L, true, "HUMANA", "Ana", "Pérez", null));
        when(donaciones.entidades()).thenReturn(List.of(
                entidad(4, true, "Comedor Sonrisas", "ONG", "Córdoba", "Córdoba"),
                entidad(5, true, "Escuela Rural N°10", "GUBERNAMENTAL", "Escobar", "Buenos Aires"),
                entidad(6, false, "Entidad dada de baja", "ONG", "Salta", "Salta")));
    }

    @Test
    void listaSoloEntidadesActivasOrdenadasYSinDatosDeContacto() throws Exception {
        mvc.perform(get("/donante/entidades"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Sección en construcción"))))
                .andExpect(content().string(containsString("Comedor Sonrisas")))
                .andExpect(content().string(containsString("Escobar, Buenos Aires")))
                // Ciudad y provincia iguales no se repiten.
                .andExpect(content().string(not(containsString("Córdoba, Córdoba"))))
                .andExpect(content().string(not(containsString("Entidad dada de baja"))))
                .andExpect(content().string(containsString("href=\"/donante/entidades/4\"")))
                .andExpect(content().string(containsString("2 entidades")));
    }

    @Test
    void buscaSinImportarTildesNiMayusculasYFiltraPorTipo() throws Exception {
        mvc.perform(get("/donante/entidades").param("q", "CORDOBA"))
                .andExpect(content().string(containsString("Comedor Sonrisas")))
                .andExpect(content().string(not(containsString("Escuela Rural"))))
                .andExpect(content().string(containsString("1 entidad")));
        mvc.perform(get("/donante/entidades").param("tipo", "GUBERNAMENTAL"))
                .andExpect(content().string(containsString("Escuela Rural")))
                .andExpect(content().string(not(containsString("Comedor Sonrisas"))));
        // Un tipo que no existe se ignora.
        mvc.perform(get("/donante/entidades").param("tipo", "INVENTADO"))
                .andExpect(content().string(containsString("2 entidades")));
        mvc.perform(get("/donante/entidades").param("q", "zzz"))
                .andExpect(content().string(containsString("No encontramos entidades con esa búsqueda")));
    }

    @Test
    void paginaDeADoce() throws Exception {
        List<Entidad> muchas = new ArrayList<>();
        for (int i = 1; i <= 13; i++) {
            muchas.add(entidad(100 + i, true, String.format("Entidad %02d", i), "ONG", "Rosario", "Santa Fe"));
        }
        when(donaciones.entidades()).thenReturn(muchas);
        mvc.perform(get("/donante/entidades"))
                .andExpect(content().string(containsString("Página 1 de 2")))
                .andExpect(content().string(containsString("/donante/entidades?page=1")))
                .andExpect(content().string(not(containsString("Entidad 13"))));
        mvc.perform(get("/donante/entidades").param("page", "1"))
                .andExpect(content().string(containsString("Entidad 13")));
    }

    @Test
    void fichaConNecesidadesAbiertasYDonacionesDelDonante() throws Exception {
        when(donaciones.entidad(4)).thenReturn(entidad(4, true, "Comedor Sonrisas", "ONG", "Córdoba", "Córdoba"));
        when(donaciones.necesidades(4)).thenReturn(List.of(
                new Necesidad(1L, "EXTRAORDINARIA", "Frazadas por la ola polar", "FRAZADA", "UNIDAD",
                        BigDecimal.valueOf(40), BigDecimal.TEN, false, null, "OLA_POLAR"),
                new Necesidad(2L, "RECURRENTE", "Arroz para el comedor", "ARROZ", "KILOGRAMO",
                        BigDecimal.valueOf(50), BigDecimal.ZERO, false, "MENSUAL", null),
                new Necesidad(3L, "RECURRENTE", "Ya cubierta", "FIDEOS_SECOS", "KILOGRAMO",
                        BigDecimal.TEN, BigDecimal.TEN, true, "SEMANAL", null)));
        var arroz = new DonacionDelDonante(9L, LocalDateTime.of(2026, 11, 10, 15, 0), "Arroz 1 kg",
                BigDecimal.valueOf(20), "KILOGRAMO", "ALIMENTOS", "ARROZ", "ENTREGADA", 4L);
        when(donaciones.donaciones(eq(1L), anyMap())).thenReturn(new Pagina<>(List.of(arroz), 1, 1, 0));

        mvc.perform(get("/donante/entidades/4"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Frazadas por la ola polar")))
                .andExpect(content().string(containsString("Ola polar")))
                .andExpect(content().string(containsString("Recibió 10 de 40 u.")))
                .andExpect(content().string(containsString("Arroz para el comedor")))
                .andExpect(content().string(containsString("Mensual")))
                .andExpect(content().string(not(containsString("Ya cubierta"))))
                .andExpect(content().string(containsString("Le hiciste 1 donación.")))
                .andExpect(content().string(containsString("href=\"/donante/donaciones/9\"")))
                .andExpect(content().string(containsString("<a href=\"/donante/entidades\">Entidades beneficiarias</a>")));
        verify(donaciones).donaciones(eq(1L), argThat(p -> "4".equals(p.get("entidadId"))));
    }

    @Test
    void siLasNecesidadesNoRespondenLaFichaSeMuestraIgual() throws Exception {
        when(donaciones.entidad(4)).thenReturn(entidad(4, true, "Comedor Sonrisas", "ONG", "Córdoba", "Córdoba"));
        when(donaciones.necesidades(4)).thenThrow(new BackendNoDisponibleException(ServicioBackend.DONACIONES, 503, null));
        when(donaciones.donaciones(eq(1L), anyMap())).thenReturn(new Pagina<>(List.of(), 0, 0, 0));
        mvc.perform(get("/donante/entidades/4"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Ahora no pudimos consultar las necesidades")))
                .andExpect(content().string(containsString("Todavía no le llegó ninguna donación tuya.")));
    }

    @Test
    void unaEntidadInactivaOInexistenteDa404() throws Exception {
        when(donaciones.entidad(6)).thenReturn(entidad(6, false, "Entidad dada de baja", "ONG", "Salta", "Salta"));
        when(donaciones.entidad(99)).thenThrow(new RecursoNoEncontradoException(ServicioBackend.DONACIONES, "No existe"));
        mvc.perform(get("/donante/entidades/6")).andExpect(status().isNotFound());
        mvc.perform(get("/donante/entidades/99")).andExpect(status().isNotFound());
    }

    @Test
    void enEntidadesUnaPersonaJuridicaTampocoVeIncentivosEnElMenu() throws Exception {
        when(donaciones.perfil(1)).thenReturn(new Perfil(1L, true, "JURIDICA", null, null, "Panadería El Sol"));
        mvc.perform(get("/donante/entidades"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("href=\"/donante/incentivos\""))));
    }
}
