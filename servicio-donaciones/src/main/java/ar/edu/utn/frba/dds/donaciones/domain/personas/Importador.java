package ar.edu.utn.frba.dds.donaciones.domain.personas;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class Importador {
    protected String nombre;
    // Solo Persona: el Importador no asigna identidad de Donante (eso lo
    // hace GestorDonantes, la única fuente de la secuencia de ids reales,
    // evitando que el alta manual y la importación masiva colisionen).
    protected List<Persona> personasImportadas = new ArrayList<>();

    public String getNombre() {
        return nombre;
    }

    public List<Persona> getPersonasImportadas() {
        return personasImportadas;
    }

    public abstract void importar(InputStream inputStream);
}
