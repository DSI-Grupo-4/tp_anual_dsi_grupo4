package ar.edu.utn.frba.dds.incentivos.domain.misiones;

import ar.edu.utn.frba.dds.incentivos.domain.insignias.Insignia;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Completitud extends Mision {
    private int cantidadCategorias;

    public Completitud(String nombre, int idMision, Insignia insigniaAsociada, LocalDate fechaAsignacion, int cantidadCategorias) {
        super(nombre, idMision, insigniaAsociada, fechaAsignacion);
        this.cantidadCategorias = cantidadCategorias;
    }

    public Completitud() {}

    @Override
    public void ejecutar() {
        if (contexto == null) return;
        int total = contexto.getTotalDonaciones();
        this.setCantidadCompletada(total);
        this.setEstaCompleta(total >= cantidadCategorias);
    }
}
