package ar.edu.utn.frba.dds.incentivos.domain.personas;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CuentaDonante {
    private List<Donante> donante = new ArrayList<>();

    public CuentaDonante() {}

    public CuentaDonante(List<Donante> donante) {
        this.donante = donante;
    }
}
