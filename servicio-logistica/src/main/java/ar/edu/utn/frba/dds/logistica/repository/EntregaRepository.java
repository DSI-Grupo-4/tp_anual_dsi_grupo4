package ar.edu.utn.frba.dds.logistica.repository;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Entrega;
import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoEntrega;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

// In-memory por ahora; esto se reemplazará por un repo con persistencia real.
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
        if (e == null) throw new NoSuchElementException("No existe la entrega con id: " + id);
        return e;
    }

    /**
     * Para que LoteService no cree una Entrega duplicada si Donaciones
     * reenvía la misma donación (ej. porque todavía figuraba "pendiente" de
     * su lado) -- sin esto, una donación ya entregada podía terminar con
     * una segunda Entrega en PENDIENTE y volver a planificarse.
     */
    public Optional<Entrega> buscarPorDonacion(Integer idDonacionAsociada) {
        return entregas.values().stream()
                .filter(e -> e.getIdDonacionAsociada().equals(idDonacionAsociada))
                .findFirst();
    }

    /**
     * Elegibles para la próxima planificación: las recién llegadas
     * (PENDIENTE) y las que una persona administradora revisó y aprobó
     * para reintentar (REPLANIFICABLE) -- ambas se tratan igual acá, la
     * diferencia es solo de trazabilidad (ver Entrega.reingresarADeposito).
     */
    public List<Entrega> obtenerPendientes() {
        return entregas.values().stream()
                .filter(e -> e.getEstadoEntrega() == EstadoEntrega.PENDIENTE
                        || e.getEstadoEntrega() == EstadoEntrega.REPLANIFICABLE)
                .toList();
    }

    public List<Entrega> obtenerTodas() {
        return new ArrayList<>(entregas.values());
    }

    public List<Entrega> obtenerPorFecha(LocalDate fecha) {
        return entregas.values().stream()
                .filter(e -> fecha.equals(e.getFecha()))
                .toList();
    }
}
