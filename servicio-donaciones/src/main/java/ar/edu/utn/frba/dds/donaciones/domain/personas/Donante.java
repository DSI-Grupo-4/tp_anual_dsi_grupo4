package ar.edu.utn.frba.dds.donaciones.domain.personas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Donacion;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Donante {
    private Persona persona;
    private List<Donacion> donaciones;
    private LocalDate ultimaActividad;

    public Donante(Persona persona) {
        this.persona = persona;
        this.ultimaActividad = getUltimaActividad();
        this.donaciones = new ArrayList<>();
    }

    public Persona getPersona() {
        return persona;
    }

    public void agregarDonacion(Donacion donacion) {
        donaciones.add(donacion);
    }

    public Integer cantidadDonaciones() {
        return donaciones.size();
    }

    public boolean realizoDonaciones() {
        return !donaciones.isEmpty();
    }
}
