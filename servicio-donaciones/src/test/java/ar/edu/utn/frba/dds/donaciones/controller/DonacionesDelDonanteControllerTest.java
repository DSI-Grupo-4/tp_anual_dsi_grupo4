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
import ar.edu.utn.frba.dds.donaciones.service.ConsultaAdministracionService;
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
    MockMvc mvc;

    @BeforeEach
    void preparar() {
        consultas = mock(ConsultaAdministracionService.class);
        donantes = mock(DonanteService.class);
        repositorio = mock(DonacionRepository.class);
        mvc = MockMvcBuilders.standaloneSetup(new DonacionesDelDonanteController(consultas, donantes, repositorio))
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
}
