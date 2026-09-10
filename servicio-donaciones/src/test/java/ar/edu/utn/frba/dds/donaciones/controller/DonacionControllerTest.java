package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import ar.edu.utn.frba.dds.donaciones.dto.CargaDonacionDTO;
import ar.edu.utn.frba.dds.donaciones.dto.DonacionDTO;
import ar.edu.utn.frba.dds.donaciones.dto.ItemDonadoDTO;
import ar.edu.utn.frba.dds.donaciones.service.DonacionService;
import ar.edu.utn.frba.dds.donaciones.service.EntidadBeneficiariaService;
import ar.edu.utn.frba.dds.donaciones.service.MatchmakingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * MockMvc en modo standalone: arma el controller a mano con sus servicios
 * mockeados, sin levantar el ApplicationContext completo ni tocar nada
 * externo al propio controller.
 */
@ExtendWith(MockitoExtension.class)
class DonacionControllerTest {

    @Mock
    private DonacionService donacionService;
    @Mock
    private MatchmakingService matchmakingService;
    @Mock
    private EntidadBeneficiariaService entidadBeneficiariaService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        DonacionController controller = new DonacionController(
                donacionService, matchmakingService, entidadBeneficiariaService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void postDonacionesAceptaLaCargaMultiItemYDevuelveUnaListaDeDonaciones() throws Exception {
        CargaDonacionDTO carga = new CargaDonacionDTO();
        carga.setDescripcion("Mudanza oficina");
        ItemDonadoDTO item = new ItemDonadoDTO();
        item.setDescripcion("Sillas");
        item.setSubcategoria("sillas");
        item.setCantidad(6);
        carga.setItems(List.of(item));

        DonacionDTO dto1 = new DonacionDTO();
        dto1.setId(1L);
        dto1.setDescripcionItem("Sillas");
        dto1.setEstadoActual(EstadoTrack.EN_DEPOSITO);

        when(donacionService.crear(any(CargaDonacionDTO.class))).thenReturn(List.of(dto1));

        mockMvc.perform(post("/api/donaciones")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(carga)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].descripcionItem").value("Sillas"));
    }

    @Test
    void putDonacionesDelegaEnActualizarSinRecrearLaDonacion() throws Exception {
        DonacionDTO cambios = new DonacionDTO();
        cambios.setDescripcionItem("Sillas renombradas");
        cambios.setCantidadAsignada(6);

        DonacionDTO actualizado = new DonacionDTO();
        actualizado.setId(1L);
        actualizado.setDescripcionItem("Sillas renombradas");

        when(donacionService.actualizar(eq(1L), any(DonacionDTO.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/donaciones/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(cambios)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcionItem").value("Sillas renombradas"));
    }
}
