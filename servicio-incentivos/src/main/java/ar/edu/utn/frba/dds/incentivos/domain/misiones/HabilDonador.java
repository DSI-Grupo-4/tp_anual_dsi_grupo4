package ar.edu.utn.frba.dds.incentivos.domain.misiones;

import ar.edu.utn.frba.dds.incentivos.domain.insignias.Insignia;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class HabilDonador extends Mision {
    private int cantidadBienes;

    public HabilDonador(String nombre, int idMision, Insignia insigniaAsociada, LocalDate fechaAsignacion, int cantidadBienes) {
        super(nombre, idMision, insigniaAsociada, fechaAsignacion);
        this.cantidadBienes = cantidadBienes;
    }

    public HabilDonador() {}

    @Override
    public void ejecutar() {
        if (contexto == null) return;
        int total = contexto.getTotalDonaciones();
        this.setCantidadCompletada(total);
        this.setEstaCompleta(total >= cantidadBienes);
    }
}
