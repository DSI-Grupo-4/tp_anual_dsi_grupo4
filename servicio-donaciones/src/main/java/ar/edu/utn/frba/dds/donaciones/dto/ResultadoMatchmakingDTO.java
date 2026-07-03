package ar.edu.utn.frba.dds.donaciones.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResultadoMatchmakingDTO {
    private List<EntidadCandidataDTO> porCompatibilidad;
    private List<EntidadCandidataDTO> porSubatencion;
    private List<EntidadCandidataDTO> interseccion;
}
