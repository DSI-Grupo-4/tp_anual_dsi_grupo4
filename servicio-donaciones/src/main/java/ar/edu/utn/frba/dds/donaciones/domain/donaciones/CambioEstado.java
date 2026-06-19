package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import lombok.Getter;

@Getter
public class CambioEstado {
    private final EstadoTrack estadoNuevo;
    private final Fecha fechaCambio;
    private final String justificacion;

    public CambioEstado(EstadoTrack estadoNuevo, String justificacion) {
        this.estadoNuevo = estadoNuevo;
        this.justificacion = justificacion;
        this.fechaCambio = null;
    }
}
