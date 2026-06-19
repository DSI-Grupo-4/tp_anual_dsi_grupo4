package ar.edu.utn.frba.dds.incentivos.domain.misiones;

import ar.edu.utn.frba.dds.incentivos.domain.insignias.Insignia;
import ar.edu.utn.frba.dds.incentivos.domain.personas.Donante;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class Racha extends Mision {
    private int cantidadDonaciones;
    private int rachaActual;

    public Racha(String nombre, int idMision, Insignia insigniaAsociada, LocalDate fechaAsignacion) {
        super(nombre, idMision, insigniaAsociada, fechaAsignacion);
    }

    @Override
    public boolean completoMision(Donante donante) {
        rachaActual = calcularRacha(donante);
        return rachaActual >= this.cantidadDonaciones;
    }

    @Override
    public float progresoMision(Donante donante) {
        rachaActual = calcularRacha(donante);
        return (float) rachaActual / this.cantidadDonaciones;
    }
