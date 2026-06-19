package ar.edu.utn.frba.dds.incentivos.domain.misiones;

import ar.edu.utn.frba.dds.incentivos.domain.insignias.Insignia;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DonacionesExitosas extends Mision {
    private int cantidadDonaciones;

    public DonacionesExitosas(String nombre, int idMision, Insignia insigniaAsociada, LocalDate fechaAsignacion, int cantidadDonaciones) {
        super(nombre, idMision, insigniaAsociada, fechaAsignacion);
        this.cantidadDonaciones = cantidadDonaciones;
    }

    public DonacionesExitosas() {}

    @Override
    public void ejecutar() {
        if (contexto == null) return;
        int total = contexto.getTotalDonaciones();
        this.setCantidadCompletada(total);
        this.setEstaCompleta(total >= cantidadDonaciones);
    }
}
