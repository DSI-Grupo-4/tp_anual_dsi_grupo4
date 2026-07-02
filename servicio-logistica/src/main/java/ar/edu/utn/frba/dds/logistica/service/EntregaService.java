package ar.edu.utn.frba.dds.logistica.service;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoEntrega;
import ar.edu.utn.frba.dds.logistica.dto.EntregaDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EntregaService {

    private final List<Entrega> entregas = new ArrayList<>();

    public Entrega obtenerPorId(Integer id) {
        return entregas.stream()
                .filter(e -> e.getIdEntrega().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No existe la entrega con id: " + id));
    }

    public void guardar(Entrega entrega) {
        entregas.add(entrega);
    }

    public void cambiarEstado(Integer id, EstadoEntrega nuevoEstado) {
        Entrega entrega = obtenerPorId(id);
        entrega.cambiarEstado(nuevoEstado);
    }

    public EntregaDTO obtenerEstadoEntrega(Integer id) {
        Entrega entrega = obtenerPorId(id);

        return new EntregaDTO(
                entrega.getIdEntrega(),
                entrega.getEstadoEntrega().name()
        );
    }
}
