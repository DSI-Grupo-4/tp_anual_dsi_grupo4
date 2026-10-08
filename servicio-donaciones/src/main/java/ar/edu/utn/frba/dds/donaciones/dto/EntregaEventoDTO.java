package ar.edu.utn.frba.dds.donaciones.dto;

import lombok.Getter;
import lombok.Setter;

// Subconjunto de la Entrega de servicio-logistica: sólo lo que necesitamos
// para aplicar el evento del lado de Donaciones (el resto de los campos del
// JSON se ignoran, Jackson no falla por propiedades desconocidas).
@Getter
@Setter
public class EntregaEventoDTO {
    private Integer idDonacionAsociada;
    private String justificacionFallo;
}
