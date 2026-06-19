package ar.edu.utn.frba.dds.incentivos.domain.personas;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GestorDonante {

    private static GestorDonante instancia;

    private List<Donante> donantes;

    private GestorDonante() {
        this.donantes = new ArrayList<>();
    }

    public static GestorDonante getInstancia() {
        if (instancia == null) {
            instancia = new GestorDonante();
        }
        return instancia;
    }

    public void agregarDonante(Donante donante) {
        donantes.add(donante);
    }
}