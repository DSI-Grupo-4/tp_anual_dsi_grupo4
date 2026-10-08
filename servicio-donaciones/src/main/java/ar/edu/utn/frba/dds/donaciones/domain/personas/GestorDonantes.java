package ar.edu.utn.frba.dds.donaciones.domain.personas;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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
     * trajo el importador indicado. Si el email ya está registrado, se
     * actualiza la Persona existente en vez de crear un donante duplicado
     * (requerimiento explícito de la importación masiva).
     */
    public List<Donante> importarDonantes(String nombreImportador) {
        return importadores.stream()
                .filter(imp -> imp.getNombre().equals(nombreImportador))
                .findFirst()
                .map(imp -> imp.getPersonasImportadas().stream()
                        .map(this::registrarOActualizarDonante)
                        .toList())
                .orElse(List.of());
    }

    private Donante registrarOActualizarDonante(Persona persona) {
        String email = emailDe(persona);
        if (email != null) {
            Optional<Donante> existente = buscarPorEmail(email);
            if (existente.isPresent()) {
                existente.get().setPersona(persona);
                existente.get().registrarActividad();
                return existente.get();
            }
        }
        return registrarDonante(persona);
    }

    private Optional<Donante> buscarPorEmail(String email) {
        return donantesRegistrados.stream()
                .filter(d -> email.equalsIgnoreCase(emailDe(d.getPersona())))
                .findFirst();
    }

    private String emailDe(Persona persona) {
        if (persona == null) {
            return null;
        }
        return persona.getMediosContacto().stream()
                .filter(m -> m.getTipo() == TipoContacto.EMAIL)
                .map(MedioContacto::getValor)
                .findFirst()
                .orElse(null);
    }
}
