package ar.edu.utn.frba.dds.donaciones.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DonacionPendienteDTO {
    private Integer idDonacion;
    private Integer entidadBeneficiariaAsociadaID;
    private DireccionDTO direccionDestino;

    // Sin @JsonProperty: el campo serializa como "pesoKG", que es lo que
    // espera DonacionDTO del lado de Logística (ver smoke test 2026-10-08 —
    // el "PesoKG" con P mayúscula que tenía antes llegaba null al otro lado
    // y rompía PlanificacionPropia con NPE).
    private Integer pesoKG;

    private Integer volumenM3;
    private Integer alturaM;
}
