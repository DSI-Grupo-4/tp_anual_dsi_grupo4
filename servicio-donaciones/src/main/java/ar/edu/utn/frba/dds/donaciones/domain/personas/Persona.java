package ar.edu.utn.frba.dds.donaciones.domain.personas;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.spi.ToolProvider.findFirst;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
    } //devuelve el medio de contacto preferido en caso de haber
}