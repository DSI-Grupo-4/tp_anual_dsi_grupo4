package ar.edu.utn.frba.dds.logistica.dto;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Direccion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DonacionDTO {
    private Integer idDonacion;
    private Integer entidadBeneficiariaAsociadaID;
    private Direccion direccionDestino;
    private Integer pesoKG;
    private Integer volumenM3;
    private Integer alturaM;
}
