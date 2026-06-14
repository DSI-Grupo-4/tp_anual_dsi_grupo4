package ar.edu.utn.frba.dds.donaciones.domain.personas;

import java.util.List;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Donante {
    private Persona persona;
    public List<Donacion> donaciones;

    public Donante(Persona persona) {
        this.persona = persona;
    }

    public Persona getPersona() {
        return persona;
    }
}
