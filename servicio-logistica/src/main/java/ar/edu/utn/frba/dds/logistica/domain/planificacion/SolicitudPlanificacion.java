package ar.edu.utn.frba.dds.logistica.domain.planificacion;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class SolicitudPlanificacion {
    private EstadoPlanificacion estado;
    private List <Lote> donacionesAEntregar;


}
