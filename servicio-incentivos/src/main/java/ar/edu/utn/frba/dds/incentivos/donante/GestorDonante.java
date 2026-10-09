package ar.edu.utn.frba.dds.incentivos.donante;

import ar.edu.utn.frba.dds.incentivos.repository.DonanteRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class GestorDonante {

    private static GestorDonante instancia;

    private final List<Donante> donantes;

    // Inyectado al boot por PersistenciaConfigurer -- null en los tests
    // unitarios que no levantan contexto de Spring (se degrada a memoria).
    private DonanteRepository donanteRepository;

    private GestorDonante() {
        this.donantes = new ArrayList<>();
    }

    public static synchronized GestorDonante getInstance() {
        if (instancia == null) {
            instancia = new GestorDonante();
        }
        return instancia;
    }

    public synchronized void configurarPersistencia(DonanteRepository donanteRepository) {
        this.donanteRepository = donanteRepository;
        donantes.clear();
        donantes.addAll(donanteRepository.findAll());
    }

    public void verificarVigenciaMisiones() {
        listarDonantes().forEach(donante -> {
            donante.verificarVigenciaMisiones();
            guardar(donante);
        });
    }

    /** Identidad recibida desde el perfil de Donaciones o su actividad. No agrega donaciones. */
    public synchronized Donante obtenerDonante(Long id) {
        return donantes.stream()
                .filter(donante -> donante.getId().equals(id))
                .findFirst()
                .orElseGet(() -> {
                    Donante nuevo = new Donante(id);
                    donantes.add(nuevo);
                    if (donanteRepository != null) {
                        donanteRepository.save(nuevo);
                    }
                    return nuevo;
                });
    }

    public Donante obtenerDonante(Long id, String nombre) {
        Donante donante = obtenerDonante(id);
        donante.actualizarNombreSiFalta(nombre);
        return donante;
    }

    /** Busca perfiles conocidos; la recuperación del servicio dueño se hace en el controller. */
    public synchronized Donante buscarDonante(Long id) {
        return donantes.stream()
                .filter(donante -> donante.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No existe el donante " + id + " en Incentivos"));
    }

    public synchronized List<Donante> listarDonantes() {
        return List.copyOf(donantes);
    }

    /** Persiste el estado actual del donante (progreso, historial, etc). No-op si no hay persistencia configurada. */
    public void guardar(Donante donante) {
        if (donanteRepository != null) {
            donanteRepository.save(donante);
        }
    }
}
