package ar.edu.utn.frba.dds.donaciones.domain.personas;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Component
public class GestorDonantes {
    private List<Donante> donantesRegistrados = new ArrayList<>();
    private List<Importador> importadores = new ArrayList<>();
    private Long siguienteId = 1L;

    // Índice por email (lowercase) -- buscarPorEmail() escaneaba toda la
    // lista por cada donante a registrar; con un CSV de 20000 filas eso es
    // ~20000 * n/2 comparaciones (O(n²)), el cuello de botella real de la
    // importación masiva (confirmado en vivo: ~16s tanto antes como después
    // de recortar la respuesta del endpoint, que no tocaba este costo).
    private final Map<String, Donante> donantesPorEmail = new HashMap<>();

    public List<Donante> getDonantesRegistrados() {
        return donantesRegistrados;
    }

    public Donante registrarDonante(Persona persona) {
        Donante donante = new Donante(siguienteId++, persona);
        donantesRegistrados.add(donante);
        indexarEmail(donante);
        return donante;
    }

    public Donante buscarPorId(Long id) {
        return donantesRegistrados.stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No existe el donante " + id));
    }

    public void eliminar(Long id) {
        donantesRegistrados.stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .ifPresent(d -> donantesPorEmail.remove(claveEmail(emailDe(d.getPersona()))));
        donantesRegistrados.removeIf(d -> d.getId().equals(id));
    }

    public void agregarImportador(Importador importador) {
        importadores.removeIf(i -> i.getNombre().equals(importador.getNombre()));
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
                // Mismo email -> misma clave en el índice, no hace falta reindexar.
                existente.get().setPersona(persona);
                existente.get().registrarActividad();
                return existente.get();
            }
        }
        return registrarDonante(persona);
    }

    public void actualizarContactos(Donante donante, List<MedioContacto> contactos) {
        donantesPorEmail.remove(claveEmail(emailDe(donante.getPersona())), donante);
        donante.getPersona().setMediosContacto(new ArrayList<>(contactos));
        indexarEmail(donante);
    }

    private Optional<Donante> buscarPorEmail(String email) {
        return Optional.ofNullable(donantesPorEmail.get(claveEmail(email)));
    }

    private void indexarEmail(Donante donante) {
        String email = emailDe(donante.getPersona());
        if (email != null) {
            donantesPorEmail.put(claveEmail(email), donante);
        }
    }

    private String claveEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(java.util.Locale.ROOT);
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
