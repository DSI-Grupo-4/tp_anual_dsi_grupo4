package ar.edu.utn.frba.dds.logistica.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResultadoPlanificacionDTO {
    private List<RutaDTO> rutas;
}
