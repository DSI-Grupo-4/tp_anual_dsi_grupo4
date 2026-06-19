package ar.edu.utn.frba.dds.incentivos.domain.misiones;

import ar.edu.utn.frba.dds.incentivos.domain.insignias.Insignia;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SueldoDonaciones extends Mision {
    private int cantidadFinancias;

    public SueldoDonaciones(String nombre, int idMision, Insignia insigniaAsociada, LocalDate fechaAsignacion, int cantidadFinancias) {
        super(nombre, idMision, insigniaAsociada, fechaAsignacion);
        this.cantidadFinancias = cantidadFinancias;
    }

    public SueldoDonaciones() {}

    @Override
    public void ejecutar() {
        if (contexto == null) return;
        int total = contexto.getTotalDonaciones();
        this.setCantidadCompletada(total);
        this.setEstaCompleta(total >= cantidadFinancias);
    }
}
