package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Fecha {
    private int dia;
    private int mes;
    private int anio;

    public Fecha(int dia, int mes, int anio) {
        this.dia = dia;
        this.mes = mes;
        this.anio = anio;
    }
}
