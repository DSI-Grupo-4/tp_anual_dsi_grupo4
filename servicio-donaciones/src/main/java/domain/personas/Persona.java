package domain.personas;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class Persona {
    private List<MedioContacto> mediosContacto;

    public Persona() {
        this.mediosContacto = new ArrayList<>();
    }

    public void agregarMedio(MedioContacto medio) {
        mediosContacto.add(medio);
    }

    public void elimiinarMedio(MedioContacto medio) {
        mediosContacto.remove(medio);
    }

    public Optional<MedioContacto> medioPreferido() {
        return mediosContacto.stream()
                .filter(MedioContacto::esPreferido)
                .findFirst();
    }
}
