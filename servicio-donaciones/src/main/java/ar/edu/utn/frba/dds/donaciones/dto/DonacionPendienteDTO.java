package ar.edu.utn.frba.dds.donaciones.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DonacionPendienteDTO {
    private Integer idDonacion;
    private Integer entidadBeneficiariaAsociadaID;
    private DireccionDTO direccionDestino;

    @JsonProperty("PesoKG")
    private Integer pesoKG;

    private Integer volumenM3;
    private Integer alturaM;
}
