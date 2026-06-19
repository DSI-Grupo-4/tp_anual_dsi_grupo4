package ar.edu.utn.frba.dds.incentivos.domain.misiones;

import ar.edu.utn.frba.dds.incentivos.domain.insignias.Insignia;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Racha extends Mision {
    private int cantidadMeses;
    private int rachaActual;

    public Racha(String nombre, int idMision, Insignia insigniaAsociada, LocalDate fechaAsignacion, int cantidadMeses) {
        super(nombre, idMision, insigniaAsociada, fechaAsignacion);
        this.cantidadMeses = cantidadMeses;
    }

    public Racha() {}

    @Override
    public void ejecutar() {
        if (contexto == null) return;
        this.rachaActual = contexto.getRachaActual();
        this.setCantidadCompletada(rachaActual);
        this.setEstaCompleta(rachaActual >= cantidadMeses);
    }
}
