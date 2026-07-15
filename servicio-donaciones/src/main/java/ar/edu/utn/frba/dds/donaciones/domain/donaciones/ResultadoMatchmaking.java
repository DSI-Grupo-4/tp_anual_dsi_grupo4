package ar.edu.utn.frba.dds.donaciones.domain.donaciones;

import ar.edu.utn.frba.dds.donaciones.domain.personas.EntidadBeneficiaria;
import lombok.Getter;

import java.util.List;

@Getter
public class ResultadoMatchmaking {
    private final List<EntidadBeneficiaria> porCompatibilidad;
    private final List<EntidadBeneficiaria> porSubatencion;
    private final List<EntidadBeneficiaria> interseccion;

    public ResultadoMatchmaking(
            List<EntidadBeneficiaria> porCompatibilidad,
            List<EntidadBeneficiaria> porSubatencion,
            List<EntidadBeneficiaria> interseccion) {
        this.porCompatibilidad = List.copyOf(porCompatibilidad);
        this.porSubatencion = List.copyOf(porSubatencion);
        this.interseccion = List.copyOf(interseccion);
    }
}
