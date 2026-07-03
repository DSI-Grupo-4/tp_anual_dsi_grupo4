package ar.edu.utn.frba.dds.logistica.dto;

import ar.edu.utn.frba.dds.logistica.domain.rutas.Direccion;
import lombok.Getter;

@Getter
public class DonacionDTO {
    private Integer idDonacion;
    private Integer entidadBeneficiariaAsociadaID;
    private Direccion direccionDestino;
    private Integer PesoKG;
    private Integer volumenM3;
    private Integer alturaM;
}
