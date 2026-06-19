package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TimeStamp {
    private EstadoDonacion estado;
    private LocalDateTime fecha;
    private String justificacion;

    public TimeStamp(
            EstadoDonacion estado,
            String justificacion) {
        this.estado = estado;
        this.fecha = LocalDateTime.now();
        this.justificacion = justificacion;
    }
}