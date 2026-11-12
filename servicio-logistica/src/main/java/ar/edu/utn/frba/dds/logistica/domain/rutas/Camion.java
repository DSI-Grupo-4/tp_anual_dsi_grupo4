package ar.edu.utn.frba.dds.logistica.domain.rutas;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

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

    // Última ubicación informada del camión (null hasta que informe una). La fecha va en UTC.
    private Double latitud;
    private Double longitud;
    private LocalDateTime ubicacionActualizadaEn;
    // true si la posición la generó el simulador de la demo y no el chofer
    private Boolean ubicacionSimulada;

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

    /**
     * Registra la ubicación del camión.
     *
     * @throws IllegalArgumentException si las coordenadas no existen (latitud fuera de -90..90 o longitud fuera de -180..180)
     */
    public void reportarUbicacion(double latitud, double longitud, LocalDateTime momentoUtc, boolean simulada) {
        if (Double.isNaN(latitud) || latitud < -90 || latitud > 90) {
            throw new IllegalArgumentException("La latitud debe estar entre -90 y 90");
        }
        if (Double.isNaN(longitud) || longitud < -180 || longitud > 180) {
            throw new IllegalArgumentException("La longitud debe estar entre -180 y 180");
        }
        this.latitud = latitud;
        this.longitud = longitud;
        this.ubicacionActualizadaEn = momentoUtc;
        this.ubicacionSimulada = simulada;
    }

    public boolean tieneUbicacion() {
        return latitud != null && longitud != null;
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
