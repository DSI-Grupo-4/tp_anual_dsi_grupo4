package ar.edu.utn.frba.dds.incentivos.donacion;

import ar.edu.utn.frba.dds.incentivos.misiones.Categoria;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DatosDonacion {

    private LocalDate fecha;
    private Categoria categoria;
    private int cantidadBienes;
    private boolean donacionExitosa;

    public DatosDonacion(LocalDate fecha, Categoria categoria, int cantidadBienes, boolean donacionExitosa) {
        this.fecha = fecha;
        this.categoria = categoria;
        this.cantidadBienes = cantidadBienes;
        this.donacionExitosa = donacionExitosa;
    }
}
