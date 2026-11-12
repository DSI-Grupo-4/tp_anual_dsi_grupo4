package ar.edu.utn.frba.dds.donaciones.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.edu.utn.frba.dds.donaciones.domain.categorias.Categoria;
import ar.edu.utn.frba.dds.donaciones.domain.categorias.Subcategoria;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import ar.edu.utn.frba.dds.donaciones.dto.DonacionDTO;
import ar.edu.utn.frba.dds.donaciones.dto.PaginaDTO;
import ar.edu.utn.frba.dds.donaciones.exception.GlobalExceptionHandler;
import ar.edu.utn.frba.dds.donaciones.repository.DonacionRepository;
import ar.edu.utn.frba.dds.donaciones.dto.TimeStampDTO;
import ar.edu.utn.frba.dds.donaciones.service.ConsultaAdministracionService;
import ar.edu.utn.frba.dds.donaciones.service.DonacionService;
import ar.edu.utn.frba.dds.donaciones.service.DonanteService;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DonacionesDelDonanteControllerTest {

    ConsultaAdministracionService consultas;
    DonanteService donantes;
    DonacionRepository repositorio;
    DonacionService donacionService;
    MockMvc mvc;

    @BeforeEach
    void preparar() {
        consultas = mock(ConsultaAdministracionService.class);
        donantes = mock(DonanteService.class);
        repositorio = mock(DonacionRepository.class);
        donacionService = mock(DonacionService.class);
        mvc = MockMvcBuilders.standaloneSetup(new DonacionesDelDonanteController(consultas, donantes, repositorio, donacionService))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void filtraSiemprePorElDonanteDeLaRuta() throws Exception {
        var donacion = new DonacionDTO();
        donacion.setId(7L);
        donacion.setDonanteId(3L);
        when(consultas.donaciones(eq(""), eq(EstadoTrack.ENTREGADA), eq(Categoria.ALIMENTOS), eq(Subcategoria.ARROZ),
                eq(3L), isNull(), eq(0), eq(5))).thenReturn(new PaginaDTO<>(List.of(donacion), 1, 1, 0));

        mvc.perform(get("/api/donantes/3/donaciones")
                        .param("q", "  ")
                        .param("estado", "ENTREGADA").param("categoria", "ALIMENTOS").param("subcategoria", "ARROZ")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(7))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void limitaElTamanioDePagina() throws Exception {
        when(consultas.donaciones(any(), any(), any(), any(), anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(new PaginaDTO<>(List.<DonacionDTO>of(), 0, 0, 0));
        mvc.perform(get("/api/donantes/3/donaciones").param("size", "5000")).andExpect(status().isOk());
        verify(consultas).donaciones("", null, null, null, 3L, null, 0, 100);
    }

    @Test
    void donanteInexistenteDevuelve404() throws Exception {
        when(donantes.buscarPorId(99L)).thenThrow(new NoSuchElementException("Donante no encontrado"));
        mvc.perform(get("/api/donantes/99/donaciones")).andExpect(status().isNotFound());
        verify(consultas, never()).donaciones(any(), any(), any(), any(), anyLong(), any(), anyInt(), anyInt());
    }

    @Test
    void resumenIncluyeTodosLosEstados() throws Exception {
        when(repositorio.count(org.mockito.ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion>>any()))
                .thenReturn(2L);
        var resultado = mvc.perform(get("/api/donantes/3/donaciones/resumen")).andExpect(status().isOk());
        for (EstadoTrack estado : EstadoTrack.values()) {
            resultado.andExpect(jsonPath("$." + estado.name()).value(2));
        }
    }

    @Test
    void resumenDeDonanteInexistenteDevuelve404() throws Exception {
        when(donantes.buscarPorId(99L)).thenThrow(new NoSuchElementException("Donante no encontrado"));
        mvc.perform(get("/api/donantes/99/donaciones/resumen")).andExpect(status().isNotFound());
    }

    @Test
    void buscaPorTextoEnLosBienes() throws Exception {
        when(consultas.donaciones(any(), any(), any(), any(), anyLong(), any(), anyInt(), anyInt()))
                .thenReturn(new PaginaDTO<>(List.<DonacionDTO>of(), 0, 0, 0));
        mvc.perform(get("/api/donantes/3/donaciones").param("q", " arroz ")).andExpect(status().isOk());
        verify(consultas).donaciones("arroz", null, null, null, 3L, null, 0, 20);
    }

    private DonacionDTO donacionDe(long donanteId) {
        var donacion = new DonacionDTO();
        donacion.setId(7L);
        donacion.setDonanteId(donanteId);
        return donacion;
    }

    @Test
    void detalleEHistorialDeUnaDonacionPropia() throws Exception {
        when(donacionService.obtenerPorId(7L)).thenReturn(donacionDe(3L));
        var cambio = new TimeStampDTO();
        cambio.setEstado(EstadoTrack.EN_DEPOSITO);
        when(donacionService.obtenerHistorial(7L)).thenReturn(List.of(cambio));

        mvc.perform(get("/api/donantes/3/donaciones/7")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(7));
        mvc.perform(get("/api/donantes/3/donaciones/7/historial")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("EN_DEPOSITO"));
    }

    @Test
    void unaDonacionDeOtroDonanteRespondeComoInexistente() throws Exception {
        when(donacionService.obtenerPorId(7L)).thenReturn(donacionDe(5L));
        mvc.perform(get("/api/donantes/3/donaciones/7")).andExpect(status().isNotFound());
        mvc.perform(get("/api/donantes/3/donaciones/7/historial")).andExpect(status().isNotFound());
        verify(donacionService, never()).obtenerHistorial(anyLong());
    }
}
