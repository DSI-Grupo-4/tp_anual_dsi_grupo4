package ar.edu.utn.frba.dds.incentivos.domain.insignias;

import ar.edu.utn.frba.dds.incentivos.domain.misiones.Mision;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Insignia {
    private String Nombre;
    private String imagen;
    private Insignia descripcionInsignia;
    private LocalDate fechaAsignacion;

    public Insignia() {}

    public Insignia(String nombre, String imagen, LocalDate fechaAsignacion) {
        this.Nombre = nombre;
        this.imagen = imagen;
        this.fechaAsignacion = fechaAsignacion;
    }

    public boolean completarMision() {
        return true;
    }

    public float progrsoMision() {
        return 1.0f;
    }

    public Insignia completaMision() {
        return this;
    }
}
