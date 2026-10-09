package ar.edu.utn.frba.dds.logistica.service;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Ciudad;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Direccion;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import ar.edu.utn.frba.dds.logistica.domain.rutas.Provincia;
import ar.edu.utn.frba.dds.logistica.dto.DonacionDTO;
import ar.edu.utn.frba.dds.logistica.repository.EntregaRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoteServiceTest {

    private final EntregaRepository entregaRepository = new EntregaRepository();
    private final LoteService loteService = new LoteService(entregaRepository);

    private DonacionDTO donacion(Integer idDonacion) {
        Direccion direccion = new Direccion("Falsa", "123", new Ciudad("CABA", new Provincia("Buenos Aires")));
        return new DonacionDTO(idDonacion, 10, direccion, 10, 1, 1);
    }

    @Test
    void recibirLoteCreaUnaEntregaPorDonacionNueva() {
        List<Entrega> creadas = loteService.recibirLote(List.of(donacion(1), donacion(2)));

        assertThat(creadas).hasSize(2);
        assertThat(entregaRepository.obtenerTodas()).hasSize(2);
    }

    @Test
    void recibirLoteEsIdempotentePorDonacion() {
        // Regresión: si Donaciones reenvía la misma donación (ej. porque de
        // su lado todavía figuraba pendiente), no debe crearse una segunda
        // Entrega -- antes una donación ya entregada podía terminar
        // duplicada en PENDIENTE y volver a planificarse.
        loteService.recibirLote(List.of(donacion(1)));

        List<Entrega> segundoEnvio = loteService.recibirLote(List.of(donacion(1), donacion(2)));

        assertThat(segundoEnvio).hasSize(1); // solo la donación 2 es nueva
        assertThat(entregaRepository.obtenerTodas()).hasSize(2); // nunca hay 2 entregas para la donación 1
    }

    @Test
    void recibirLoteRechazaMasDeCienDonaciones() {
        List<DonacionDTO> loteGrande = java.util.stream.IntStream.rangeClosed(1, 101)
                .mapToObj(this::donacion)
                .toList();

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> loteService.recibirLote(loteGrande))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
