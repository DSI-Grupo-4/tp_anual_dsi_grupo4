package ar.edu.utn.frba.dds.donaciones.controller;

import ar.edu.utn.frba.dds.donaciones.client.IncentivosClient;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.*;
import ar.edu.utn.frba.dds.donaciones.domain.necesidades.Necesidad;
import ar.edu.utn.frba.dds.donaciones.domain.personas.Donante;
import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import ar.edu.utn.frba.dds.donaciones.domain.personas.GestorDonantes;
import ar.edu.utn.frba.dds.donaciones.dto.*;
import ar.edu.utn.frba.dds.donaciones.exception.GlobalExceptionHandler;
import ar.edu.utn.frba.dds.donaciones.integracion.PublicadorEventosPort;
import ar.edu.utn.frba.dds.donaciones.repository.*;
import ar.edu.utn.frba.dds.donaciones.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.media.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controllers y servicios reales; sólo se simulan los repositorios JPA (con
 * un mock respaldado por una lista en memoria, no una base real) y los
 * adaptadores de red.
 */
class FlujoDonacionesTest {
    private MockMvc mvc;
    private GestorDonaciones gestor;
    private List<SolicitudDonacion> solicitudesGuardadas;
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    private ObjectNode ejemplo(Class<?> tipo) throws Exception {
        return (ObjectNode) mapper.readTree(tipo.getAnnotation(Schema.class).example());
    }

    @BeforeEach void preparar() throws Exception {
        List<Donacion> donacionesGuardadas = new ArrayList<>();
        AtomicLong siguienteDonacionId = new AtomicLong(1L);
        DonacionRepository donacionRepository = mock(DonacionRepository.class);
        when(donacionRepository.save(any())).thenAnswer(inv -> {
            Donacion d = inv.getArgument(0);
            if (d.getId() == null) d.setId(siguienteDonacionId.getAndIncrement());
            donacionesGuardadas.removeIf(x -> x.getId().equals(d.getId()));
            donacionesGuardadas.add(d);
            return d;
        });
        when(donacionRepository.findAll()).thenAnswer(inv -> List.copyOf(donacionesGuardadas));
        when(donacionRepository.findById(any())).thenAnswer(inv -> {
            Long id = inv.getArgument(0);
            return donacionesGuardadas.stream().filter(x -> x.getId().equals(id)).findFirst();
        });
        doAnswer(inv -> {
            Long id = inv.getArgument(0);
            donacionesGuardadas.removeIf(x -> x.getId().equals(id));
            return null;
        }).when(donacionRepository).deleteById(any());

        solicitudesGuardadas = new ArrayList<>();
        AtomicLong siguienteSolicitudId = new AtomicLong(1L);
        SolicitudDonacionRepository solicitudDonacionRepository = mock(SolicitudDonacionRepository.class);
        when(solicitudDonacionRepository.save(any())).thenAnswer(inv -> {
            SolicitudDonacion s = inv.getArgument(0);
            if (s.getId() == null) s.setId(siguienteSolicitudId.getAndIncrement());
            solicitudesGuardadas.removeIf(x -> x.getId().equals(s.getId()));
            solicitudesGuardadas.add(s);
            return s;
        });

        gestor = new GestorDonaciones(new Deposito(), donacionRepository, solicitudDonacionRepository);

        List<Donante> donantesGuardados = new ArrayList<>();
        AtomicLong siguienteDonanteId = new AtomicLong(1L);
        DonanteRepository donanteRepository = mock(DonanteRepository.class);
        when(donanteRepository.save(any())).thenAnswer(inv -> {
            Donante d = inv.getArgument(0);
            if (d.getId() == null) d.setId(siguienteDonanteId.getAndIncrement());
            donantesGuardados.removeIf(x -> x.getId().equals(d.getId()));
            donantesGuardados.add(d);
            return d;
        });
        when(donanteRepository.findAll()).thenAnswer(inv -> List.copyOf(donantesGuardados));
        when(donanteRepository.findById(any())).thenAnswer(inv -> {
            Long id = inv.getArgument(0);
            return donantesGuardados.stream().filter(x -> x.getId().equals(id)).findFirst();
        });
        GestorDonantes donantes = new GestorDonantes(donanteRepository);

        List<EntidadBeneficiaria> entidadesGuardadas = new ArrayList<>();
        AtomicLong siguienteEntidadId = new AtomicLong(1L);
        EntidadBeneficiariaRepository entidadBeneficiariaRepository = mock(EntidadBeneficiariaRepository.class);
        when(entidadBeneficiariaRepository.save(any())).thenAnswer(inv -> {
            EntidadBeneficiaria e = inv.getArgument(0);
            if (e.getId() == null) e.setId(siguienteEntidadId.getAndIncrement());
            entidadesGuardadas.removeIf(x -> x.getId().equals(e.getId()));
            entidadesGuardadas.add(e);
            return e;
        });
        when(entidadBeneficiariaRepository.findAll()).thenAnswer(inv -> List.copyOf(entidadesGuardadas));
        when(entidadBeneficiariaRepository.findById(any())).thenAnswer(inv -> {
            Long id = inv.getArgument(0);
            return entidadesGuardadas.stream().filter(x -> x.getId().equals(id)).findFirst();
        });
        EntidadBeneficiariaService entidades = new EntidadBeneficiariaService(entidadBeneficiariaRepository, gestor);

        List<Necesidad> necesidadesGuardadas = new ArrayList<>();
        AtomicLong siguienteNecesidadId = new AtomicLong(1L);
        NecesidadRepository necesidadRepository = mock(NecesidadRepository.class);
        when(necesidadRepository.save(any())).thenAnswer(inv -> {
            Necesidad n = inv.getArgument(0);
            if (n.getId() == null) {
                var idField = Necesidad.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(n, siguienteNecesidadId.getAndIncrement());
            }
            necesidadesGuardadas.removeIf(x -> x.getId().equals(n.getId()));
            necesidadesGuardadas.add(n);
            return n;
        });
        when(necesidadRepository.findById(any())).thenAnswer(inv -> {
            Long id = inv.getArgument(0);
            return necesidadesGuardadas.stream().filter(x -> x.getId().equals(id)).findFirst();
        });

        EventoAplicadoRepository eventoAplicadoRepository = mock(EventoAplicadoRepository.class);
        ItemDonadoRepository itemDonadoRepository = mock(ItemDonadoRepository.class);
        when(itemDonadoRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        DonacionService donaciones = new DonacionService(gestor, donantes, mock(PublicadorEventosPort.class),
                mock(IncentivosClient.class), eventoAplicadoRepository, necesidadRepository, itemDonadoRepository);
        NecesidadService necesidadService = new NecesidadService(necesidadRepository, entidades);
        mvc = MockMvcBuilders.standaloneSetup(
                new DonanteController(new DonanteService(donantes)),
                new EntidadBeneficiariaController(entidades),
                new NecesidadController(necesidadService),
                new DonacionController(donaciones,new MatchmakingService(entidades,gestor),entidades),
                new AsignacionController(new AsignacionService(entidades,gestor)))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(post("/api/donantes/humanos").contentType("application/json").content(ejemplo(PersonaHumanaDTO.class).toString())).andExpect(status().isCreated());
        mvc.perform(post("/api/entidades").contentType("application/json").content(ejemplo(EntidadBeneficiariaDTO.class).toString())).andExpect(status().isCreated());
    }
    @Test void ejemplosSwaggerFuncionanEnElFlujoCompleto() throws Exception {
        mvc.perform(post("/api/entidades/1/necesidades/extraordinarias").contentType("application/json").content(ejemplo(NecesidadExtraordinariaDTO.class).toString())).andExpect(status().isCreated());
        mvc.perform(post("/api/entidades/1/necesidades/recurrentes").contentType("application/json").content(ejemplo(NecesidadRecurrenteDTO.class).toString())).andExpect(status().isCreated());
        mvc.perform(post("/api/donaciones").contentType("application/json").content(ejemplo(CargaDonacionDTO.class).toString()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].cantidadAsignada").value(2.5)).andExpect(jsonPath("$[0].solicitudOrigenId").value(1));
        mvc.perform(post("/api/asignaciones/candidatas").contentType("application/json").content(ejemplo(SolicitudAsignacionDTO.class).toString()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.porCompatibilidad[0].entidadBeneficiariaId").value(1));
        mvc.perform(put("/api/donaciones/1").contentType("application/json").content(ejemplo(ActualizarDonacionDTO.class).toString())).andExpect(status().isOk());
        mvc.perform(get("/api/donaciones/1/candidatas")).andExpect(status().isOk())
                .andExpect(jsonPath("$.porCompatibilidad[0].entidadBeneficiariaId").value(1));
        mvc.perform(post("/api/donaciones/1/asignar").contentType("application/json").content(ejemplo(AsignarEntidadDTO.class).toString())).andExpect(status().isOk());
        mvc.perform(patch("/api/donaciones/1/estado").contentType("application/json").content(ejemplo(CambioEstadoDTO.class).toString())).andExpect(status().isOk());
        mvc.perform(put("/api/donaciones/1").contentType("application/json").content(ejemplo(ActualizarDonacionDTO.class).toString())).andExpect(status().isConflict());
    }
    @ParameterizedTest
    @ValueSource(strings={"categoria","subcategoria","unidadMedida","cantidad","condicion","fechaVencimiento","nullItem"})
    void rechazaCargaInvalidaSinRegistrarParcialmente(String campo) throws Exception {
        ObjectNode carga=ejemplo(CargaDonacionDTO.class);
        ObjectNode item=(ObjectNode)carga.withArray("items").get(1);
        switch(campo) {
            case "categoria" -> item.put("categoria","MOBILIARIO");
            case "subcategoria" -> item.put("subcategoria","INEXISTENTE");
            case "unidadMedida" -> item.put("unidadMedida","LITRO");
            case "cantidad" -> item.put("cantidad",0);
            case "condicion" -> item.put("condicion","USADO");
            case "fechaVencimiento" -> item.remove("fechaVencimiento");
            case "nullItem" -> carga.withArray("items").addNull();
        }
        mvc.perform(post("/api/donaciones").contentType("application/json").content(carga.toString())).andExpect(status().isBadRequest());
        assertThat(gestor.getDonaciones()).isEmpty();
        assertThat(solicitudesGuardadas).isEmpty();
        assertThat(gestor.getDeposito().itemsDisponibles()).isEmpty();
    }
    @Test void condicionEsObligatoriaYLasUnidadesNoSeFraccionan() throws Exception {
        ObjectNode carga=ejemplo(CargaDonacionDTO.class);
        ObjectNode item=(ObjectNode)carga.withArray("items").get(0);
        item.remove("condicion");
        mvc.perform(post("/api/donaciones").contentType("application/json").content(carga.toString())).andExpect(status().isBadRequest());
        item.put("condicion","NUEVO");item.put("cantidad",0.5);
        mvc.perform(post("/api/donaciones").contentType("application/json").content(carga.toString())).andExpect(status().isBadRequest());
    }
    @Test void editarGrupoNoHomogeneoNoModificaOriginal() throws Exception {
        mvc.perform(post("/api/donaciones").contentType("application/json").content(ejemplo(CargaDonacionDTO.class).toString())).andExpect(status().isCreated());
        ObjectNode cambios=mapper.createObjectNode();cambios.set("items",ejemplo(CargaDonacionDTO.class).get("items"));
        mvc.perform(put("/api/donaciones/1").contentType("application/json").content(cambios.toString())).andExpect(status().isBadRequest());
        assertThat(gestor.buscarPorId(1L).getCantidadAsignada()).isEqualByComparingTo("3");
        assertThat(gestor.getDeposito().itemsDisponibles()).hasSize(2);
        assertThat(gestor.buscarPorId(1L).getSolicitudOrigen().getItems()).hasSize(2);
    }
    @Test void necesidadesValidanUnidadYEnumYNoSeMezclanTipos() throws Exception {
        ObjectNode n=ejemplo(NecesidadExtraordinariaDTO.class);n.put("unidadMedida","KILOGRAMO");
        mvc.perform(post("/api/entidades/1/necesidades/extraordinarias").contentType("application/json").content(n.toString())).andExpect(status().isBadRequest());
        n.put("unidadMedida","UNIDAD");
        String respuesta=mvc.perform(post("/api/entidades/1/necesidades/extraordinarias").contentType("application/json").content(n.toString())).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id=mapper.readTree(respuesta).get("id").asLong();
        n.put("tipo","RECURRENTE");n.put("periodicidad","SEMANAL");
        mvc.perform(put("/api/entidades/1/necesidades/"+id).contentType("application/json").content(n.toString())).andExpect(status().isBadRequest());
    }
}
