package ar.edu.utn.frba.dds.logistica.domain.rutas;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "camion")
public class Camion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_camion")
    private Integer idCamion;

    @Column(name = "patente", nullable = false, unique = true, length = 10)
    private String patente;

    @Column(name = "capacidad_volumen_m3", nullable = false)
    private Integer capacidadVolumenM3;

    @Column(name = "altura_m", nullable = false)
    private Integer alturaM;

    @Column(name = "capacidad_carga_kg", nullable = false) // es carga en KG, no M3
    private Integer capacidadCargaKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_camion", nullable = false)
    private EstadoCamion estadoCamion;

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
