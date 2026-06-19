package ar.edu.utn.frba.dds.incentivos.domain.misiones;

import ar.edu.utn.frba.dds.incentivos.domain.donaciones.Donacion;
import ar.edu.utn.frba.dds.incentivos.domain.insignias.Insignia;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public abstract class Mision {
    private String nombre;
    private Integer idMision;
    private Insignia insigniaAsociada;
    private LocalDate fechaAsignacion;
    private int cantidadCompletada;
    private boolean estaCompleta;
    protected Donacion contexto;

    protected Mision(String nombre, int idMision, Insignia insigniaAsociada, LocalDate fechaAsignacion) {
        this.nombre = nombre;
        this.idMision = idMision;
        this.insigniaAsociada = insigniaAsociada;
        this.fechaAsignacion = fechaAsignacion;
    }

    protected Mision() {}

    public abstract void ejecutar();

    public Insignia cumplirMision() {
        return insigniaAsociada;
    }
}
