package ar.edu.utn.frba.dds.incentivos.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class NotificacionEventoDTO {
    private String tipo;
    private Long donanteId;
    private String mensaje;
    private LocalDate fecha;

    public NotificacionEventoDTO(String tipo, Long donanteId, String mensaje, LocalDate fecha) {
        this.tipo = tipo;
        this.donanteId = donanteId;
        this.mensaje = mensaje;
        this.fecha = fecha;
    }
}
