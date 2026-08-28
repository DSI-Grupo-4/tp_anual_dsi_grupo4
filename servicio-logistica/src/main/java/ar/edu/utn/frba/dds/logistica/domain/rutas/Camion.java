package ar.edu.utn.frba.dds.logistica.domain.rutas;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Camion {
    private Integer idCamion;
    private String patente;
    private Integer capacidadVolumenM3;
    private Integer alturaM;
    private Integer capacidadCargaKg; // renombrado: es carga en KG, no M3
    private EstadoCamion estadoCamion; // renombrado para que coincida con CamionDTO y sea consistente

    public Camion() {}

    public Camion(Integer idCamion, String patente, Integer capacidadVolumenM3,
                  Integer alturaM, Integer capacidadCargaKg, EstadoCamion estadoCamion) {
        this.idCamion = idCamion;
        this.patente = patente;
        this.capacidadVolumenM3 = capacidadVolumenM3;
        this.alturaM = alturaM;
        this.capacidadCargaKg = capacidadCargaKg;
        this.estadoCamion = estadoCamion;
    }

    public void cambiarEstado(EstadoCamion nuevoEstado) {
        this.estadoCamion = nuevoEstado;
    }

    public boolean puedeCargar(int pesoKg, int volumenM3, int alturaM) {
        return pesoKg <= this.capacidadCargaKg
                && volumenM3 <= this.capacidadVolumenM3
                && alturaM <= this.alturaM;
    }
}
