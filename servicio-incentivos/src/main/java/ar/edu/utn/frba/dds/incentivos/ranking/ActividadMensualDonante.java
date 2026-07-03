package ar.edu.utn.frba.dds.incentivos.ranking;

import ar.edu.utn.frba.dds.incentivos.donante.Donante;
import lombok.Getter;

@Getter
public class ActividadMensualDonante {

    private final Donante donanteAsociado;
    private int cantidad;

    public ActividadMensualDonante(Donante donanteAsociado) {
        this.donanteAsociado = donanteAsociado;
        this.cantidad = 0;
    }

    public void incrementar() {
        cantidad++;
    }
}
