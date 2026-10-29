package ar.edu.utn.frba.dds.logistica.domain.rutas;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "camiones")
public class Camion {

    // Id numérico: lo asigna GeneradorIds (secuencia en la colección "secuencias") al guardar si es null.
    @Id
    private Integer idCamion;

    @Indexed(unique = true)
    private String patente;

    private Integer capacidadVolumenM3;

    private Integer alturaM;

    private Integer capacidadCargaKg; // es carga en KG, no M3

    @Indexed
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
