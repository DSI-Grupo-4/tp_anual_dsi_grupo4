package ar.edu.utn.frba.dds.incentivos.donante;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class GestorDonante {

    private static GestorDonante instancia;

    private final List<Donante> donantes;

    private GestorDonante() {
        this.donantes = new ArrayList<>();
    }

    public static synchronized GestorDonante getInstance() {
        if (instancia == null) {
            instancia = new GestorDonante();
        }
        return instancia;
    }

    public void verificarVigenciaMisiones() {
        donantes.forEach(Donante::verificarVigenciaMisiones);
    }

    /**
     * Alta perezosa: Incentivos no tiene su propio alta de donantes --
     * Donaciones es quien es dueño de esa identidad, así que el primer
     * POST /actividad-donacion que llega para un id nuevo es la señal de
     * que ese donante existe. Usar esto SOLO ahí, nunca en una consulta de
     * lectura (ver buscarDonante).
     */
    public Donante obtenerDonante(Long id) {
        return donantes.stream()
                .filter(donante -> donante.getId().equals(id))
                .findFirst()
                .orElseGet(() -> {
                    Donante nuevo = new Donante(id);
                    donantes.add(nuevo);
                    return nuevo;
                });
    }

    public Donante obtenerDonante(Long id, String nombre) {
        Donante donante = obtenerDonante(id);
        donante.actualizarNombreSiFalta(nombre);
        return donante;
    }

    /**
     * Para endpoints de lectura (y la modificación de visibilidad de una
     * insignia): un id que nunca mandó actividad no debe fabricar un
     * donante vacío -- antes GET /{id}/metricas, /{id}/misiones,
     * /{id}/insignias y PATCH .../visibilidad devolvían 200 con progreso
     * vacío para cualquier id, sin importar si existía.
     */
    public Donante buscarDonante(Long id) {
        return donantes.stream()
                .filter(donante -> donante.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No existe el donante " + id + " en Incentivos"));
    }

    public List<Donante> listarDonantes() {
        return List.copyOf(donantes);
    }
}
