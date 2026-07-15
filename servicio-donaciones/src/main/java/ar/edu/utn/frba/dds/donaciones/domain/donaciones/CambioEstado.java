package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class CambioEstado {
    private final EstadoTrack estadoNuevo;
    private final LocalDateTime fechaCambio;
    private final String justificacion;

    public CambioEstado(EstadoTrack estadoNuevo, String justificacion) {
        this.estadoNuevo = estadoNuevo;
        this.fechaCambio = LocalDateTime.now();
        this.justificacion = justificacion;
    }
}
