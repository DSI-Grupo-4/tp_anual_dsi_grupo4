package ar.edu.utn.frba.dds.donaciones.domain.personas;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.Fecha;
import ar.edu.utn.frba.dds.donaciones.domain.donaciones.SolicitudDonacion;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Donante {
    private Persona personaAsociada;
    private List<SolicitudDonacion> donacionesRealizadas = new ArrayList<>();
    private Fecha ultimaActividad;

    public Donante(Persona personaAsociada) {
        this.personaAsociada = personaAsociada;
    }

    public void crearSolicitudDonacion() {
        donacionesRealizadas.add(new SolicitudDonacion());
    }
}
