package ar.edu.utn.frba.dds.logistica.dto;

import ar.edu.utn.frba.dds.logistica.domain.rutas.EstadoCamion;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CamionDTO {
    private Integer idCamion;
    private String patente;
    private EstadoCamion estadoCamion;
}
