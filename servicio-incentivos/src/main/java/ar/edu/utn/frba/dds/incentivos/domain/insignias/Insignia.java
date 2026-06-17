package ar.edu.utn.frba.dds.incentivos.domain.insignias;

import java.time.LocalDate;

import ar.edu.utn.frba.dds.incentivos.domain.personas.Donante;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Insignia {

    private String nombre;
    private String imagen;
    private LocalDate fechaEmision;
    private Donante donanteAsociado;

    public Insignia(String nombre, String imagen, LocalDate fechaEmision, Donante donanteAsociado) {
        this.nombre = nombre;
        this.imagen = imagen;
        this.fechaEmision = fechaEmision;
        this.donanteAsociado = donanteAsociado;
    }

}
