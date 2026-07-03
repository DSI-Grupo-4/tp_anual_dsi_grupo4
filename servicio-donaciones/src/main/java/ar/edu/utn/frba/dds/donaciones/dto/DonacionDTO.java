package ar.edu.utn.frba.dds.donaciones.dto;

import ar.edu.utn.frba.dds.donaciones.domain.donaciones.EstadoTrack;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DonacionDTO {
    private Long id;
    private String descripcionItem;
    private Integer cantidadAsignada;
    private EstadoTrack estadoActual;
    private Long entidadBeneficiariaId;
    private Long necesidadId;
    // Id del donante que hizo la donación. Necesario para poder notificarlo
    // cuando la donación sea asignada (Entrega 2) y para registrar su
    // actividad (usado en la notificación por inactividad > 20 días).
    private Long donanteId;
    private Integer pesoKg;
    private Integer volumenM3;
    private Integer alturaM;
}
