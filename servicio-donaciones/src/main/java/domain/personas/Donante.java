package domain.personas;

import domain.donaciones.Donacion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Donante {
    private Persona persona;
    private List<Donacion> donaciones;
    private LocalDate fechaUltimaInteraccion;

    public Donante(Persona persona) {
        this.persona = persona;
        this.donaciones = new ArrayList<>();
        this.fechaUltimaInteraccion = LocalDate.now();
    }

    public Persona getPersona() {
        return persona;
    }

    public List<Donacion> getDonaciones() {
        return donaciones;
    }

    public LocalDate getFechaUltimaInteraccion() {
        return fechaUltimaInteraccion;
    }

    public void setFechaUltimaInteraccion(LocalDate fechaUltimaInteraccion) {
        this.fechaUltimaInteraccion = fechaUltimaInteraccion;
    }
}
