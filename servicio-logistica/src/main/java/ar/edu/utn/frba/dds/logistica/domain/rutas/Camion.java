package ar.edu.utn.frba.dds.logistica.domain.rutas;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Camion {
    private Integer idCamion;
    private String patente;
    private Integer capacidadVolumenM3;
    private Integer alturaM;
    private Integer capacidadCargaM3;
    private EstadoCamion estado;

    public Camion(){};

    public void cambiarEstado(){};
}
