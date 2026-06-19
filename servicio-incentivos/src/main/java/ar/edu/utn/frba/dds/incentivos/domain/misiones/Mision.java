package ar.edu.utn.frba.dds.incentivos.domain.misiones;

import java.time.LocalDate;

import ar.edu.utn.frba.dds.incentivos.domain.insignias.Insignia;
import ar.edu.utn.frba.dds.incentivos.domain.personas.Donante;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public abstract class Mision {

    private String nombre;
    private Integer idMision;
    private Insignia insigniaAsociada;
    private LocalDate fechaAsignacion;

    public Mision(String nombre, int idMision, Insignia insigniaAsociada, LocalDate fechaAsignacion) {
        this.nombre = nombre;
        this.idMision = idMision;
        this.insigniaAsociada = insigniaAsociada;
        this.fechaAsignacion = fechaAsignacion;
    }

    public abstract boolean completoMision(Donante donante);

    public abstract float progresoMision(Donante donante);

    public Insignia cumplirMision() {
        return insigniaAsociada;
    }
}

