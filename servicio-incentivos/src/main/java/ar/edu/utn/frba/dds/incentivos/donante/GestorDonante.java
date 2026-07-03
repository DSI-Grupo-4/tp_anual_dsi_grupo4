package ar.edu.utn.frba.dds.incentivos.donante;

import java.util.ArrayList;
import java.util.List;

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
}
