package ar.edu.utn.frba.dds.incentivos.progreso;

import ar.edu.utn.frba.dds.incentivos.misiones.Insignia;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ProgresoInsignia {

    private Insignia insigniaAsociada;
    private LocalDate fechaObtencion;
    private boolean visible;

    public ProgresoInsignia(Insignia insigniaAsociada) {
        this.insigniaAsociada = insigniaAsociada;
        this.fechaObtencion = LocalDate.now();
        this.visible = true;
    }

    public void marcarVisible() {
        this.visible = true;
    }

    public void ocultarInsignia() {
        this.visible = false;
    }
}
