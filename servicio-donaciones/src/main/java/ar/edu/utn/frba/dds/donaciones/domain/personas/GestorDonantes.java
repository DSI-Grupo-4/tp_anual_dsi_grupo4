package ar.edu.utn.frba.dds.donaciones.domain.personas;

import ar.edu.utn.frba.dds.donaciones.repository.DonanteRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Component
public class GestorDonantes {
    private final DonanteRepository donanteRepository;
    private List<Importador> importadores = new ArrayList<>();

    // Índice por email (lowercase) -- buscarPorEmail() escaneaba toda la
    // lista por cada donante a registrar; con un CSV de 20000 filas eso es
    // ~20000 * n/2 comparaciones (O(n²)), el cuello de botella real de la
    // importación masiva (confirmado en vivo: ~16s tanto antes como después
    // de recortar la respuesta del endpoint, que no tocaba este costo). Se
    // mantiene en memoria (en vez de una query por fila) para no reintroducir
    // ese costo ahora que hay una base real de por medio; se recarga al boot
    // y se mantiene al día en cada alta/actualización.
    private final Map<String, Donante> donantesPorEmail = new HashMap<>();

    public GestorDonantes(DonanteRepository donanteRepository) {
        this.donanteRepository = donanteRepository;
    }

    @PostConstruct
    void cargarIndiceDeEmails() {
        donanteRepository.findAll().forEach(this::indexarEmail);
    }

    /** Persiste mutaciones hechas sobre un Donante ya existente (registrarActividad, marcarNotificadoPorInactividad, etc). */
    public Donante guardar(Donante donante) {
        return donanteRepository.save(donante);
    }

    public List<Donante> getDonantesRegistrados() {
        return donanteRepository.findAll();
    }

    public Donante registrarDonante(Persona persona) {
        Donante donante = new Donante(null, persona);
        donante = donanteRepository.save(donante);
        indexarEmail(donante);
        return donante;
    }

    public Donante buscarPorId(Long id) {
        return donanteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No existe el donante " + id));
    }

    public void eliminar(Long id) {
        donanteRepository.findById(id)
                .ifPresent(d -> donantesPorEmail.remove(claveEmail(emailDe(d.getPersona()))));
        donanteRepository.deleteById(id);
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
                return donanteRepository.save(existente.get());
            }
        }
        return registrarDonante(persona);
    }

    public void actualizarContactos(Donante donante, List<MedioContacto> contactos) {
        donantesPorEmail.remove(claveEmail(emailDe(donante.getPersona())), donante);
        donante.getPersona().setMediosContacto(new ArrayList<>(contactos));
        donante = donanteRepository.save(donante);
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
