package ar.edu.utn.frba.dds.incentivos.donacion;

import ar.edu.utn.frba.dds.incentivos.consultor.Beneficiario;
import lombok.Getter;
import java.math.BigDecimal;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DatosDonacion {

    private Long donacionId;
    private LocalDate fecha;
    // Nombre de la categoría de bienes donados (Alimentos/Mobiliario/Vestimenta,
    // tal como la modela el Servicio de Donaciones) -- no confundir con
    // misiones.Categoria (Colaborador/Sostenedor/Transformador), que es el
    // rango del donante, un concepto distinto. Solo se usa para la misión
    // "Completitud" (variedad de categorías donadas).
    private String categoriaBien;
    private BigDecimal cantidadBienes;
    private boolean donacionExitosa;
    private Beneficiario beneficiario;

    public DatosDonacion(LocalDate fecha, String categoriaBien, BigDecimal cantidadBienes,
                          boolean donacionExitosa, Beneficiario beneficiario) {
        if (fecha == null || cantidadBienes == null || cantidadBienes.signum() <= 0)
            throw new IllegalArgumentException("fecha y cantidadBienes positiva son obligatorias");
        this.fecha = fecha;
        this.categoriaBien = categoriaBien;
        this.cantidadBienes = cantidadBienes;
        this.donacionExitosa = donacionExitosa;
        this.beneficiario = beneficiario;
    }
}
