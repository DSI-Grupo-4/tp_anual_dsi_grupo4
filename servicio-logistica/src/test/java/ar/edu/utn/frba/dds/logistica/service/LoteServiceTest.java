package ar.edu.utn.frba.dds.logistica.service;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Camion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Ciudad;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Direccion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoCamion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Provincia;
import ar.edu.utn.frba.dds.logistica.dto.DonacionDTO;
import ar.edu.utn.frba.dds.logistica.repository.EntregaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Prueba la lógica de LoteService con un repositorio en memoria (sin MongoDB levantado):
// los métodos default del repositorio (guardar, buscarPorDonacion, obtenerTodas) corren de verdad
// y las operaciones de Mongo (save, findAll, findByIdDonacionAsociada) se simulan con un Map.
class LoteServiceTest {

    private Map<Integer, Entrega> almacen;
    private EntregaRepository entregaRepository;
    private LoteService loteService;

    @BeforeEach
    void preparar() {
        almacen = new LinkedHashMap<>();
        entregaRepository = mock(EntregaRepository.class, CALLS_REAL_METHODS);
        int[] ultimoId = {0};
        when(entregaRepository.save(any(Entrega.class))).thenAnswer(invocacion -> {
            Entrega entrega = invocacion.getArgument(0);
            if (entrega.getIdEntrega() == null) {
                entrega.setIdEntrega(++ultimoId[0]);
            }
            almacen.put(entrega.getIdEntrega(), entrega);
            return entrega;
        });
        when(entregaRepository.findByIdDonacionAsociada(anyInt())).thenAnswer(invocacion -> {
            Integer idDonacion = invocacion.getArgument(0);
            return almacen.values().stream()
                    .filter(e -> e.getIdDonacionAsociada().equals(idDonacion))
                    .findFirst();
        });
        when(entregaRepository.findAll()).thenAnswer(invocacion -> new ArrayList<>(almacen.values()));
        loteService = new LoteService(entregaRepository);
    }

    private DonacionDTO donacion(Integer idDonacion) {
        return donacion(idDonacion, 10);
    }

    private DonacionDTO donacion(Integer idDonacion, Integer idEntidad) {
        Direccion direccion = new Direccion("Falsa", "123", new Ciudad("CABA", new Provincia("Buenos Aires")));
        return new DonacionDTO(idDonacion, idEntidad, direccion, 10, 1, 1);
    }

    @Test
    void recibirLoteCreaUnaEntregaPorDonacionNueva() {
        List<Entrega> creadas = loteService.recibirLote(List.of(donacion(1), donacion(2)));

        assertThat(creadas).hasSize(2);
        assertThat(entregaRepository.obtenerTodas()).hasSize(2);
    }

    @Test
    void recibirLoteEsIdempotentePorDonacionMientrasSigaDisponible() {
        // Regresión: si Donaciones reenvía la misma donación (ej. porque se
        // reasignó a otra entidad tras una entrega fallida), no debe
        // crearse una segunda Entrega -- se actualiza la que ya existe.
        loteService.recibirLote(List.of(donacion(1)));

        List<Entrega> segundoEnvio = loteService.recibirLote(List.of(donacion(1), donacion(2)));

        assertThat(segundoEnvio).hasSize(2); // donación 1 actualizada + donación 2 creada
        assertThat(entregaRepository.obtenerTodas()).hasSize(2); // nunca hay 2 entregas para la donación 1
    }

    @Test
    void recibirLoteActualizaLaEntidadYDireccionSiLaEntregaSigueDisponible() {
        // Reasignación legítima: la donación se le había asignado a la
        // entidad 10, falló la entrega, Donaciones la reasignó a la 20.
        loteService.recibirLote(List.of(donacion(1, 10)));
        loteService.recibirLote(List.of(donacion(1, 20)));

        Entrega entrega = entregaRepository.buscarPorDonacion(1).orElseThrow();
        assertThat(entrega.getIdEntidadBeneficiariaAsociada()).isEqualTo(20);
    }

    @Test
    void recibirLoteIgnoraUnaDonacionCuyaEntregaYaEstaEnCurso() {
        List<Entrega> primero = loteService.recibirLote(List.of(donacion(1, 10)));
        Camion camion = new Camion(1, "AA123BB", 10, 2, 500, EstadoCamion.ASIGNADO);
        primero.get(0).asignarARuta(camion);

        List<Entrega> segundoEnvio = loteService.recibirLote(List.of(donacion(1, 20)));

        assertThat(segundoEnvio).isEmpty();
        Optional<Entrega> entrega = entregaRepository.buscarPorDonacion(1);
        assertThat(entrega.orElseThrow().getIdEntidadBeneficiariaAsociada()).isEqualTo(10); // no se pisó
    }

    @Test
    void recibirLoteRechazaMasDeCienDonaciones() {
        List<DonacionDTO> loteGrande = java.util.stream.IntStream.rangeClosed(1, 101)
                .mapToObj(this::donacion)
                .toList();

        assertThatThrownBy(() -> loteService.recibirLote(loteGrande))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
