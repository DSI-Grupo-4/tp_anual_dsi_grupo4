package ar.edu.utn.frba.dds.donaciones.repository;

import domain.personas.Donante;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class DonanteRepository {

    private final Map<Integer, Donante> donantes = new ConcurrentHashMap<>();

    public void guardar(Integer id, Donante donante) {
        donantes.put(id, donante);
    }

    public List<Donante> listarTodos() {
        return new ArrayList<>(donantes.values());
    }
}
