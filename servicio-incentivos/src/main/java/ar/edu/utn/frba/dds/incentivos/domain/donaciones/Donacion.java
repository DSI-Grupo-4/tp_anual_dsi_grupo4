package ar.edu.utn.frba.dds.incentivos.domain.donaciones;

import ar.edu.utn.frba.dds.incentivos.domain.misiones.Mision;
import ar.edu.utn.frba.dds.incentivos.domain.personas.CategoriasDonante;
import ar.edu.utn.frba.dds.incentivos.domain.personas.CuentaDonante;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Donacion {
    private int id;
    private CuentaDonante idDonante;
    private CategoriasDonante categoriaDonada;
    private Mision insigniaActual;
    private int totalDonaciones;
    private int totalOrganizaciones;
    private int rachaActual;
    private Mision misionActual;
    private LocalDateTime fechaUltimaDonaion;

    public Donacion() {}

    public void completarMision() {
        if (misionActual == null) return;
        misionActual.setContexto(this);
        misionActual.ejecutar();
    }

    public void verAvanceMision() {
        if (misionActual == null) return;
        misionActual.setContexto(this);
        misionActual.ejecutar();
    }

    public void inicDelgasion() {}
}
