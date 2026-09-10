package ar.edu.utn.frba.dds.donaciones.domain.personas;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Component
public class GestorDonantes {
    private List<Donante> donantesRegistrados = new ArrayList<>();
    private List<Importador> importadores = new ArrayList<>();
    private Long siguienteId = 1L;

    public List<Donante> getDonantesRegistrados() {
        return donantesRegistrados;
    }

    public Donante registrarDonante(Persona persona) {
        Donante donante = new Donante(siguienteId++, persona);
        donantesRegistrados.add(donante);
        return donante;
    }

    public Donante buscarPorId(Long id) {
        return donantesRegistrados.stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No existe el donante " + id));
    }

    public void eliminar(Long id) {
        donantesRegistrados.removeIf(d -> d.getId().equals(id));
    }

    public void agregarImportador(Importador importador) {
        importadores.add(importador);
    }

    /**
     * Registra como Donante real (con id asignado por este mismo gestor,
     * evitando la colisión que había antes entre el alta manual y el CSV,
     * que arrancaban su propia numeración por separado) a cada Persona que
     * trajo el importador indicado.
     */
    public List<Donante> importarDonantes(String nombreImportador) {
        return importadores.stream()
                .filter(imp -> imp.getNombre().equals(nombreImportador))
                .findFirst()
                .map(imp -> imp.getPersonasImportadas().stream()
                        .map(this::registrarDonante)
                        .toList())
                .orElse(List.of());
    }
}
