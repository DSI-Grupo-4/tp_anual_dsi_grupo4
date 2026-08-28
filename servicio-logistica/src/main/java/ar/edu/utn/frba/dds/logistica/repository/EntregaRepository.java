package ar.edu.utn.frba.dds.logistica.repository;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

// In-memory por ahora; en Entrega 4 esto se reemplaza por un repo con persistencia real.
@Component
public class EntregaRepository {

    private final Map<Integer, Entrega> entregas = new LinkedHashMap<>();
    private final AtomicInteger contadorId = new AtomicInteger(1);

    public Entrega guardar(Entrega entrega) {
        if (entrega.getIdEntrega() == null) {
            entrega.setIdEntrega(contadorId.getAndIncrement());
        }
        entregas.put(entrega.getIdEntrega(), entrega);
        return entrega;
    }

    public Entrega buscarPorId(Integer id) {
        Entrega e = entregas.get(id);
        if (e == null) throw new RuntimeException("No existe la entrega con id: " + id);
        return e;
    }

    public List<Entrega> obtenerPendientes() {
        return entregas.values().stream()
                .filter(e -> e.getEstadoEntrega() == ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoEntrega.PENDIENTE)
                .toList();
    }

    public List<Entrega> obtenerTodas() {
        return new ArrayList<>(entregas.values());
    }
}
